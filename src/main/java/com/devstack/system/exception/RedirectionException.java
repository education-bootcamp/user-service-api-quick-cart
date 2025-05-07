package com.devstack.system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.MOVED_PERMANENTLY)
public class RedirectionException extends RuntimeException{
    public RedirectionException(String message) {
        super(message);
    }
}
