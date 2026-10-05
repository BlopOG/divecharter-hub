package com.divecharter.hub.exceptions;

import org.springframework.http.HttpStatus;

/** 422 - the dive site is deeper than the diver's certification allows. */
public class CertificationInsufficientException extends ApiException {

    public CertificationInsufficientException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}