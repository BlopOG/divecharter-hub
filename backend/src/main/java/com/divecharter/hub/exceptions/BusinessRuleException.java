package com.divecharter.hub.exceptions;

import org.springframework.http.HttpStatus;

/** 409 - a valid request that breaks a business rule. */
public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}