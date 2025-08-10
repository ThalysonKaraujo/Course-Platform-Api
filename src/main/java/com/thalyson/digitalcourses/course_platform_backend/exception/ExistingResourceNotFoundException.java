package com.thalyson.digitalcourses.course_platform_backend.exception;

public class ExistingResourceNotFoundException extends RuntimeException {
    public ExistingResourceNotFoundException(String message) {
        super(message);
    }
}
