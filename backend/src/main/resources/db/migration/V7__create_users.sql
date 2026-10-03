CREATE TABLE users (
                       id             BIGINT       NOT NULL AUTO_INCREMENT,
                       email          VARCHAR(255) NOT NULL,
                       password_hash  VARCHAR(255) NOT NULL,
                       full_name      VARCHAR(100) NOT NULL,
                       role           VARCHAR(20)  NOT NULL,
                       cert_level     VARCHAR(30)  NOT NULL,
                       cert_agency    VARCHAR(50),
                       cert_number    VARCHAR(50),
                       cert_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
                       created_at     DATETIME(6)  NOT NULL,
                       CONSTRAINT pk_users PRIMARY KEY (id),
                       CONSTRAINT uq_users_email UNIQUE (email),
                       CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN')),
                       CONSTRAINT chk_users_cert_level CHECK (cert_level IN
                                                              ('OPEN_WATER', 'ADVANCED_OPEN_WATER', 'RESCUE_DIVER', 'DEEP_SPECIALTY', 'DIVEMASTER'))
);