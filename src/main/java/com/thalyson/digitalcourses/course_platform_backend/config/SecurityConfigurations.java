package com.thalyson.digitalcourses.course_platform_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfigurations {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> {
                    req.requestMatchers(HttpMethod.POST, "/login").permitAll();
                    req.requestMatchers(HttpMethod.POST, "/register/instructor").permitAll();
                    req.requestMatchers(HttpMethod.POST, "/register/student").permitAll();

                    req.requestMatchers(HttpMethod.GET, "/courses/**", "/modules/**").permitAll();

                    req.requestMatchers("/swagger-ui.html", "/swagger-ui/**").permitAll();
                    req.requestMatchers("/v3/api-docs/**").permitAll();
                    req.requestMatchers("/test/protected/public").permitAll();
                    req.requestMatchers("/admin/**").hasRole("ADMIN");
                    req.requestMatchers(HttpMethod.POST, "/courses", "/courses/*/modules", "/modules/*/lessons")
                            .hasAnyRole("INSTRUCTOR", "ADMIN");
                    req.requestMatchers(HttpMethod.GET, "/categories/**").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/enrollments/user/*").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/enrollments/course/*").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/enrollments/*").permitAll();
                    req.anyRequest().authenticated();
                })
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Falha na autenticação");
                        })
                        .accessDeniedHandler(customAccessDeniedHandler()))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                Map<String, String> error = new HashMap<>();
                error.put("error", "Access Denied");
                error.put("message", "Você não tem permissão para acessar este recurso.");
                error.put("path", request.getRequestURI());

                OutputStream out = response.getOutputStream();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(out, error);
                out.flush();
            }
        };
    }
}