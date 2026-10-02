CREATE TABLE dive_trips (
                            id              BIGINT        NOT NULL AUTO_INCREMENT,
                            dive_site_id    BIGINT        NOT NULL,
                            boat_name       VARCHAR(100)  NOT NULL,
                            departure_time  DATETIME(6)   NOT NULL,
                            return_time     DATETIME(6)   NOT NULL,
                            capacity        INT           NOT NULL,
                            seats_booked    INT           NOT NULL DEFAULT 0,
                            price           DECIMAL(10,2) NOT NULL,
                            status          VARCHAR(20)   NOT NULL,
                            version         BIGINT        NOT NULL DEFAULT 0,
                            CONSTRAINT pk_dive_trips PRIMARY KEY (id),
                            CONSTRAINT fk_dive_trips_site FOREIGN KEY (dive_site_id) REFERENCES dive_sites (id),
                            CONSTRAINT chk_dive_trips_capacity CHECK (capacity BETWEEN 1 AND 50),
                            CONSTRAINT chk_dive_trips_seats CHECK (seats_booked >= 0 AND seats_booked <= capacity),
                            CONSTRAINT chk_dive_trips_times CHECK (return_time > departure_time),
                            CONSTRAINT chk_dive_trips_price CHECK (price >= 0),
                            CONSTRAINT chk_dive_trips_status CHECK (status IN ('SCHEDULED', 'CANCELLED', 'COMPLETED'))
);

CREATE INDEX idx_dive_trips_status_departure ON dive_trips (status, departure_time);