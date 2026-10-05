package com.divecharter.hub.exceptions;

import org.springframework.http.HttpStatus;

/** 403 - staff have not yet verified the diver's certification card. */
public class CertificationNotVerifiedException extends ApiException {

    public CertificationNotVerifiedException() {
        super(HttpStatus.FORBIDDEN,
                "Your certification has not been verified yet. A staff member must verify it before you can book.");
    }
}