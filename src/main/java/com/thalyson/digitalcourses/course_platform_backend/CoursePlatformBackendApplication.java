package com.thalyson.digitalcourses.course_platform_backend;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(servers = {@Server(url = "https://course-platform-api-production.up.railway.app", description = "Server URL")})
@SpringBootApplication
public class CoursePlatformBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoursePlatformBackendApplication.class, args);
	}

}
