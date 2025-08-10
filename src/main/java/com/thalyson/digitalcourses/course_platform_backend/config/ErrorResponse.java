package com.thalyson.digitalcourses.course_platform_backend.config;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) {
}
