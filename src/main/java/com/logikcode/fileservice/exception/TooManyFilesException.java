package com.logikcode.fileservice.exception;

public class TooManyFilesException extends RuntimeException{

    public TooManyFilesException(String message) {
        super(message);
    }

}
