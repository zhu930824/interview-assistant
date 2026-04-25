CREATE TABLE IF NOT EXISTS voice_interview_session (
    session_id VARCHAR(36) PRIMARY KEY,
    paused TINYINT(1) DEFAULT 0,
    latest_transcript TEXT,
    current_question TEXT,
    subtitles JSON,
    ai_replies JSON,
    submitted_turns INT DEFAULT 0,
    user_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
