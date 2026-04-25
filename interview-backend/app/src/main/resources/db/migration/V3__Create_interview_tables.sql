CREATE TABLE IF NOT EXISTS interview_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    direction VARCHAR(50) NOT NULL,
    total_minutes INT NOT NULL,
    stage_durations JSON,
    follow_up_rounds INT DEFAULT 0,
    current_question_id VARCHAR(36),
    transcript TEXT,
    evaluation TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    user_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS interview_question (
    id VARCHAR(36) PRIMARY KEY,
    session_id BIGINT NOT NULL,
    stage VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id),
    FOREIGN KEY (session_id) REFERENCES interview_session(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
