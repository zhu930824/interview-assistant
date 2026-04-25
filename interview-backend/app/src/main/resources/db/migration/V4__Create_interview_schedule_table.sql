CREATE TABLE IF NOT EXISTS interview_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company VARCHAR(100),
    position VARCHAR(100),
    start_time DATETIME NOT NULL,
    meeting_link VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    source VARCHAR(50),
    raw_content TEXT,
    user_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
