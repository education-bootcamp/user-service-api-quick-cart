package com.devstack.quickcart.user_service_api.adviser;

import com.devstack.quickcart.user_service_api.exception.*;
import com.devstack.quickcart.user_service_api.util.StandardResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppWideExceptionHandler {
    @ExceptionHandler(EntryNotFoundException.class)
    public ResponseEntity<StandardResponseDto> handleEntryNotFoundException(EntryNotFoundException e) {

        return new ResponseEntity<StandardResponseDto>(
                new StandardResponseDto(404, e.getMessage(), e),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TooManyRequestException.class)
    public ResponseEntity<StandardResponseDto> handleTooManyRequestsException(TooManyRequestException e) {

        return new ResponseEntity<StandardResponseDto>(
                new StandardResponseDto(429, e.getMessage(), e),
                HttpStatus.TOO_MANY_REQUESTS);
    }
    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<StandardResponseDto> handleInternalServerException(InternalServerException e) {

        return new ResponseEntity<StandardResponseDto>(
                new StandardResponseDto(500, e.getMessage(), e),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<StandardResponseDto> handleBadRequestException(BadRequestException e) {

        return new ResponseEntity<StandardResponseDto>(
                new StandardResponseDto(400, e.getMessage(), e),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<StandardResponseDto> handleUnauthorizedException(UnauthorizedException e) {
        return new ResponseEntity<StandardResponseDto>(
                new StandardResponseDto(401, e.getMessage(), e),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<StandardResponseDto> handleDuplicateEntryException(DuplicateEntryException e) {
        return new ResponseEntity<StandardResponseDto>(
                new StandardResponseDto(409, e.getMessage(), e),
                HttpStatus.CONFLICT);
    }
    @ExceptionHandler(RedirectionException.class)
    public ResponseEntity<StandardResponseDto> handleRedirectionExceptionException(RedirectionException e) {
        return new ResponseEntity<StandardResponseDto>(
                new StandardResponseDto(301, e.getMessage(), e),
                HttpStatus.MOVED_PERMANENTLY);
    }
}
