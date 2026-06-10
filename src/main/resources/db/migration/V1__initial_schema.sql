-- ============================================================
-- V1__initial_schema.sql
-- ============================================================

CREATE TABLE roles (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    public_id   VARCHAR(20)     NOT NULL UNIQUE,
    name        VARCHAR(100)    NOT NULL UNIQUE
);

CREATE TABLE users (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    public_id   VARCHAR(20)     NOT NULL UNIQUE,
    username    VARCHAR(255)    NOT NULL UNIQUE,
    email       VARCHAR(255)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    active      BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME        NOT NULL
);

CREATE TABLE user_roles (
    user_id     BIGINT          NOT NULL,
    role_id     BIGINT          NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE permissions (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    public_id       VARCHAR(20)     NOT NULL UNIQUE,
    name            VARCHAR(100)    NOT NULL UNIQUE,
    action          VARCHAR(50)     NOT NULL,
    resource_type   VARCHAR(50)     NOT NULL
);

CREATE TABLE role_permissions (
    role_id         BIGINT          NOT NULL,
    permission_id   BIGINT          NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role       FOREIGN KEY (role_id)       REFERENCES roles(id)       ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE TABLE hotels (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    public_id   VARCHAR(20)     NOT NULL UNIQUE,
    owner_id    BIGINT          NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    address     VARCHAR(255),
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME        NOT NULL,
    CONSTRAINT fk_hotels_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE hotel_managers (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    public_id       VARCHAR(20)     NOT NULL UNIQUE,
    hotel_id        BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    scoped_role_id  BIGINT          NOT NULL,
    assigned_at     DATETIME        NOT NULL,
    UNIQUE KEY uq_hotel_user (hotel_id, user_id),
    CONSTRAINT fk_hm_hotel  FOREIGN KEY (hotel_id)       REFERENCES hotels(id) ON DELETE CASCADE,
    CONSTRAINT fk_hm_user   FOREIGN KEY (user_id)        REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_hm_role   FOREIGN KEY (scoped_role_id) REFERENCES roles(id)
);

CREATE TABLE user_permissions (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    public_id       VARCHAR(20)     NOT NULL UNIQUE,
    user_id         BIGINT          NOT NULL,
    permission_id   BIGINT          NOT NULL,
    resource_id     BIGINT,
    is_granted      BOOLEAN         NOT NULL,
    reason          VARCHAR(255),
    UNIQUE KEY uq_user_perm_resource (user_id, permission_id, resource_id),
    CONSTRAINT fk_up_user       FOREIGN KEY (user_id)       REFERENCES users(id)       ON DELETE CASCADE,
    CONSTRAINT fk_up_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE INDEX idx_hotels_owner         ON hotels(owner_id);
CREATE INDEX idx_hotel_managers_hotel ON hotel_managers(hotel_id);
CREATE INDEX idx_hotel_managers_user  ON hotel_managers(user_id);
CREATE INDEX idx_user_perms_user      ON user_permissions(user_id);