package com.huiway.activiti.exception;

import org.springframework.http.HttpStatus;

public class NotFoundError extends BaseError {

    private static final long serialVersionUID = 3162591564663217324L;

    public NotFoundError() {
        this("error.not-found");
    }

    public NotFoundError(String errorCode) {
        super(errorCode, HttpStatus.NOT_FOUND);
    }

}
