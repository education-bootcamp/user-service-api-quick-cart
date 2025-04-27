package com.devstack.quickcart.user_service_api.adviser;

import com.devstack.quickcart.user_service_api.exception.EntryNotFoundException;
import com.devstack.quickcart.user_service_api.util.StandardResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppWideExceptionHandler {
    @ExceptionHandler(EntryNotFoundException.class)
    public ResponseEntity<StandardResponseDto> handleEntryNotFoundException(EntryNotFoundException e) {
        return new ResponseEntity<>(
                 StandardResponseDto.builder()
                         .status(404)
                         .data(e)
                         .message(e.getMessage()).build(), HttpStatus.NOT_FOUND
        );
    }
}
