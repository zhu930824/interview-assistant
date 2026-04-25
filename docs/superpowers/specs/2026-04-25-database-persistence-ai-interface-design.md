# Interview Assistant - Database Persistence & AI Interface Design

## Goal

Replace in-memory ConcurrentHashMap storage with MySQL + MyBatis-Plus persistence for all modules, and refactor AI logic into interface + stub pattern for future AI service integration.

## Architecture

### AI Interface Layer

```java
// infrastructure/ai/AiService.java
public interface AiService {
    String chat(String systemPrompt, String userMessage);
    Flux<String> chatStream(String systemPrompt, String userMessage);
}
```

- `StubAiService` retains current hardcoded logic as fallback
- Future: `OpenAiService` / `ClaudeAiService` / `DeepSeekAiService` as real implementations

### Database Conventions

- Flyway migrations, incremental version numbers
- MyBatis-Plus BaseMapper, XML for complex queries
- Consistent `created_at` / `updated_at` columns
- JSON columns for list fields (keywords, strengths, risks, etc.)
- `user_id` foreign key for user-scoped data (nullable initially)

## Migration Order

Resume -> Interview -> Schedule -> Knowledge -> VoiceInterview

## Module Designs

### 1. Resume Module

**Table: resume**

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO_INCREMENT | |
| file_name | VARCHAR(255) NOT NULL | |
| file_path | VARCHAR(500) NOT NULL | |
| content_hash | VARCHAR(64) NOT NULL UNIQUE | SHA-256 dedup |
| status | VARCHAR(20) NOT NULL DEFAULT 'PENDING' | |
| retry_count | INT DEFAULT 0 | |
| progress | INT DEFAULT 0 | |
| duplicated | TINYINT(1) DEFAULT 0 | |
| candidate_name | VARCHAR(100) | |
| target_position | VARCHAR(100) | |
| analysis_summary | TEXT | |
| keywords | JSON | JSON array string |
| strengths | JSON | JSON array string |
| risks | JSON | JSON array string |
| raw_text_preview | TEXT | |
| user_id | BIGINT | Optional FK to user |
| created_at | DATETIME DEFAULT CURRENT_TIMESTAMP | |
| updated_at | DATETIME ON UPDATE CURRENT_TIMESTAMP | |

**AI Usage**: `aiService.chat("resume analyst", prompt)` -> structured analysis result

### 2. Interview Module

**Table: interview_session**

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO_INCREMENT | |
| direction | VARCHAR(50) NOT NULL | |
| total_minutes | INT NOT NULL | |
| stage_durations | JSON | Stage->minutes map |
| follow_up_rounds | INT DEFAULT 0 | |
| current_question_id | VARCHAR(36) | UUID ref |
| transcript | TEXT | |
| evaluation | TEXT | |
| status | VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS' | |
| user_id | BIGINT | |
| created_at | DATETIME | |
| updated_at | DATETIME | |

**Table: interview_question**

| Column | Type | Notes |
|--------|------|-------|
| id | VARCHAR(36) PK | UUID |
| session_id | BIGINT NOT NULL FK | |
| stage | VARCHAR(30) NOT NULL | |
| content | TEXT NOT NULL | |
| sort_order | INT NOT NULL | |
| created_at | DATETIME | |

**AI Usage**: AI generates questions by direction; AI evaluates transcript

### 3. Interview Schedule Module

**Table: interview_schedule**

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO_INCREMENT | |
| company | VARCHAR(100) | |
| position | VARCHAR(100) | |
| start_time | DATETIME NOT NULL | |
| meeting_link | VARCHAR(500) | |
| status | VARCHAR(20) NOT NULL DEFAULT 'PENDING' | |
| source | VARCHAR(50) | |
| raw_content | TEXT | Original invitation text |
| user_id | BIGINT | |
| created_at | DATETIME | |
| updated_at | DATETIME | |

**AI Usage**: AI extracts company/position/time/link from invitation text

### 4. Knowledge Base Module

**Table: knowledge_document**

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO_INCREMENT | |
| file_name | VARCHAR(255) NOT NULL | |
| file_path | VARCHAR(500) NOT NULL | |
| summary | TEXT | |
| chunks | INT DEFAULT 0 | |
| token_estimate | INT DEFAULT 0 | |
| keywords | JSON | |
| full_content | MEDIUMTEXT | Extracted full text |
| user_id | BIGINT | |
| created_at | DATETIME | |
| updated_at | DATETIME | |

**AI Usage**: RAG pattern - search docs + AI generation for answers

### 5. Voice Interview Module

**Table: voice_interview_session**

| Column | Type | Notes |
|--------|------|-------|
| session_id | VARCHAR(36) PK | UUID |
| paused | TINYINT(1) DEFAULT 0 | |
| latest_transcript | TEXT | |
| current_question | TEXT | |
| subtitles | JSON | JSON array |
| ai_replies | JSON | JSON array |
| submitted_turns | INT DEFAULT 0 | |
| user_id | BIGINT | |
| created_at | DATETIME | |
| updated_at | DATETIME | |

**AI Usage**: AI generates dynamic follow-up questions based on user response

## Implementation Notes

- Each module gets: Flyway migration SQL, Entity record, Mapper interface, Service refactored to use Mapper
- AiService interface + StubAiService implementation
- All existing Controller/DTO remain unchanged
- RedisStreamService stays as-is (InMemoryRedisStreamService for dev)
- Frontend requires no changes (same API contract)
