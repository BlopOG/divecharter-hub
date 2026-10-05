CREATE TABLE bookings (
                          id            BIGINT        NOT NULL AUTO_INCREMENT,
                          user_id       BIGINT        NOT NULL,
                          dive_trip_id  BIGINT        NOT NULL,
                          status        VARCHAR(20)   NOT NULL,
                          total_price   DECIMAL(10,2) NOT NULL,
                          booked_at     DATETIME(6)   NOT NULL,
                          CONSTRAINT pk_bookings PRIMARY KEY (id),
                          CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id),
                          CONSTRAINT fk_bookings_trip FOREIGN KEY (dive_trip_id) REFERENCES dive_trips (id),
                          CONSTRAINT chk_bookings_status CHECK (status IN ('CONFIRMED', 'CANCELLED')),
                          CONSTRAINT chk_bookings_price CHECK (total_price >= 0)
);

CREATE INDEX idx_bookings_user_trip_status ON bookings (user_id, dive_trip_id, status);