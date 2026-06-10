CREATE TABLE manager_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id VARCHAR(20) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL UNIQUE,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_manager_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_manager_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);