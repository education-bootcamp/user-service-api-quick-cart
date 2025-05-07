package com.devstack.system.adviser;

import com.devstack.system.exception.*;
import com.devstack.system.util.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppWideExceptionHandler {
    @ExceptionHandler(EntryNotFoundException.class)
    public ResponseEntity<StandardResponse> handleEntryNotFoundException(EntryNotFoundException e) {

        return new ResponseEntity<StandardResponse>(
                new StandardResponse(404, e.getMessage(), e),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TooManyRequestException.class)
    public ResponseEntity<StandardResponse> handleTooManyRequestsException(TooManyRequestException e) {

        return new ResponseEntity<StandardResponse>(
                new StandardResponse(429, e.getMessage(), e),
                HttpStatus.TOO_MANY_REQUESTS);
    }
    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<StandardResponse> handleInternalServerException(InternalServerException e) {

        return new ResponseEntity<StandardResponse>(
                new StandardResponse(500, e.getMessage(), e),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<StandardResponse> handleBadRequestException(BadRequestException e) {

        return new ResponseEntity<StandardResponse>(
                new StandardResponse(400, e.getMessage(), e),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<StandardResponse> handleUnauthorizedException(UnauthorizedException e) {
        return new ResponseEntity<StandardResponse>(
                new StandardResponse(401, e.getMessage(), e),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<StandardResponse> handleDuplicateEntryException(DuplicateEntryException e) {
        return new ResponseEntity<StandardResponse>(
                new StandardResponse(409, e.getMessage(), e),
                HttpStatus.CONFLICT);
    }
    @ExceptionHandler(RedirectionException.class)
    public ResponseEntity<StandardResponse> handleRedirectionExceptionException(RedirectionException e) {
        return new ResponseEntity<StandardResponse>(
                new StandardResponse(301, e.getMessage(), e),
                HttpStatus.MOVED_PERMANENTLY);
    }
}
