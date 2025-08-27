package com.thalyson.digitalcourses.course_platform_backend.config;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.thalyson.digitalcourses.course_platform_backend.service.TokenService;
import com.thalyson.digitalcourses.course_platform_backend.model.UserJPA;
import com.thalyson.digitalcourses.course_platform_backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository repository;


    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response , FilterChain filterChain)
            throws ServletException, IOException {

        var tokenJWT = recuperarToken(request);

        if (tokenJWT != null) {
            try {
                var subject = tokenService.getSubject(tokenJWT);

                Optional<UserJPA> optionalUser = repository.findByEmail(subject);

                if (optionalUser.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuário associado ao token não foi encontrado.");
                    return;
                }

                var user = optionalUser.get();

                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (TokenExpiredException e){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                ErrorResponse errorResponse = new ErrorResponse("Token JWT expirado.", LocalDateTime.now());

                objectMapper.registerModule(new JavaTimeModule());
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                return;
            } catch (RuntimeException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT inválido ou malformado.");
                return;
            }
        }

        filterChain.doFilter(request,response);
    }

    private String recuperarToken(HttpServletRequest request){
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            return authorizationHeader.replace("Bearer ", "").trim();
        }
        return null;
    }

    private record ErrorResponse(String message, LocalDateTime timestamp) {}
}