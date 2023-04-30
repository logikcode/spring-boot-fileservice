package com.logikcode.fileupload.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class TooManyFilesException extends RuntimeException{

    public TooManyFilesException(String message) {
        super(message);
    }

}
