CREATE TABLE dive_sites (
                            id           BIGINT       NOT NULL AUTO_INCREMENT,
                            name         VARCHAR(100) NOT NULL,
                            location     VARCHAR(150) NOT NULL,
                            max_depth_m  INT          NOT NULL,
                            difficulty   VARCHAR(20)  NOT NULL,
                            description  TEXT,
                            CONSTRAINT pk_dive_sites PRIMARY KEY (id),
                            CONSTRAINT uq_dive_sites_name UNIQUE (name),
                            CONSTRAINT chk_dive_sites_depth CHECK (max_depth_m BETWEEN 1 AND 40),
                            CONSTRAINT chk_dive_sites_difficulty CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);