package com.logikcode.fileservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalException {
    @ExceptionHandler(TooManyFilesException.class)
    public ResponseEntity<String> handleTooManyFilesException(TooManyFilesException tooManyFilesException){

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(tooManyFilesException.getMessage());
    }
}
