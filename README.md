# Interview Assistant

An end-to-end interview preparation workspace that combines resume analysis, mock interviews, interview schedule management, voice interviews, and knowledge base chat.

## Project Structure

```text
interview-backend/
  app/
interview-frontend/
docker-compose.yml
```

## Current Local Capabilities

- Resume upload, hash dedupe, async analysis, retry, progress tracking, and PDF report export
- Skill-driven mock interview sessions with staged timing, follow-up rounds, answer flow, evaluation, and report export
- Invite parsing for interview schedules, status transitions, scheduled expiration, and calendar grouping
- Knowledge document upload, chunk stats, download, and SSE streaming chat
- Voice interview session management with WebSocket subtitle loop, pause and resume, transcript submit, and PDF report export

## Tech Notes

- The current codebase is wired for local runnable mode first
- Business data is stored in memory plus local file storage under `interview-backend/app/data`
- External services like MySQL, Redis, Elasticsearch, Spring AI, and DashScope can be reintroduced on top of the current module boundaries

## Start Backend

```bash
cd interview-backend/app
mvn spring-boot:run
```

## Start Frontend

```bash
cd interview-frontend
npm install
npm run dev
```

## Optional Local Dependencies

```bash
docker compose up -d
```

## Main Endpoints

- `/api/resumes`
- `/api/interviews`
- `/api/interview-schedules`
- `/api/knowledge-bases`
- `/api/voice-interviews`
- `/swagger-ui.html`
