ALTER TABLE hotels
    ADD COLUMN phone VARCHAR(20),
    ADD COLUMN email VARCHAR(100),
    ADD COLUMN website VARCHAR(255),
    ADD COLUMN description TEXT,
    ADD COLUMN star_rating DECIMAL(3,1),
    ADD CONSTRAINT chk_star_rating CHECK (star_rating BETWEEN 0.0 AND 5.0);