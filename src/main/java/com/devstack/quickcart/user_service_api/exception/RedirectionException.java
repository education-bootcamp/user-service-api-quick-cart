package com.devstack.quickcart.user_service_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.MOVED_PERMANENTLY)
public class RedirectionException extends RuntimeException{
    public RedirectionException(String message) {
        super(message);
    }
}
