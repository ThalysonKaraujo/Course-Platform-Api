package com.thalyson.digitalcourses.course_platform_backend.exception;

public class AccessDeniedByBusinessException extends RuntimeException {

    public AccessDeniedByBusinessException(String message) {
        super(message);
    }
}
