package com.divecharter.hub.exceptions;

import org.springframework.http.HttpStatus;

/** 409 - no seats left on the boat. */
public class TripFullException extends ApiException {

    public TripFullException(String boatName) {
        super(HttpStatus.CONFLICT, "The trip on " + boatName + " is fully booked");
    }
}