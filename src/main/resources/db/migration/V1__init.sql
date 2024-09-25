CREATE TABLE IF NOT EXISTS diary_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    food_name VARCHAR(255) NOT NULL,
    food_points DOUBLE NOT NULL,
    amount DOUBLE NOT NULL,
    date DATE NOT NULL,
    INDEX idx_date (date)
);
