package com.thalyson.digitalcourses.course_platform_backend.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DadosErroValidacao>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<DadosErroValidacao> errors = fieldErrors.stream()
                .map(DadosErroValidacao::new)
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(errors);
    }


    @ExceptionHandler(ExistingResourceNotFoundException.class)
    public ResponseEntity<StandardErrorDto> handleExistingResourceNotFoundException(ExistingResourceNotFoundException ex, WebRequest request) {
        log.warn("Recurso não encontrado via validação: {}", ex.getMessage());
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardErrorDto error = new StandardErrorDto(
                Instant.now(),
                status.value(),
                "Recurso não encontrado",
                ex.getMessage(),
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(error);
    }


    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<StandardErrorDto> handleHandlerMethodValidationException(HandlerMethodValidationException ex, WebRequest request) {
        log.warn("Falha de validação em argumento de método: {}", ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorMessage = "Falha de validação em argumentos.";
        StandardErrorDto error = new StandardErrorDto(
                Instant.now(),
                status.value(),
                "Requisição inválida",
                errorMessage,
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<StandardErrorDto> handleEmailAlreadyInUseException(EmailAlreadyInUseException ex, WebRequest request) {
        log.warn("Tentativa de registro com email já em uso: {}", ex.getMessage());
        HttpStatus status = HttpStatus.CONFLICT;
        StandardErrorDto error = new StandardErrorDto(
                Instant.now(),
                status.value(),
                "Conflito de Recurso",
                ex.getMessage(),
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<StandardErrorDto> handleDuplicateResourceException(DuplicateResourceException ex, WebRequest request) {
        log.warn("Tentativa de criar recurso duplicado: {}", ex.getMessage());
        HttpStatus status = HttpStatus.CONFLICT;
        StandardErrorDto error = new StandardErrorDto(
                Instant.now(),
                status.value(),
                "Conflito de Recurso",
                ex.getMessage(),
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardErrorDto> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        log.error("Erro de integridade de dados: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.CONFLICT;
        String errorMessage = "Erro de integridade de dados.";
        if (ex.getMessage() != null && ex.getMessage().contains("duplicate key value violates unique constraint")) {
            errorMessage = "Violação de unicidade: Um recurso com o valor fornecido já existe.";
        } else if (ex.getMessage() != null && ex.getMessage().contains("null value in column")) {
            errorMessage = "Erro de preenchimento: Um campo obrigatório está nulo.";
        }
        StandardErrorDto error = new StandardErrorDto(
                Instant.now(),
                status.value(),
                "Conflito de dados",
                errorMessage,
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardErrorDto> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Argumento ilegal: {}", ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardErrorDto error = new StandardErrorDto(
                Instant.now(),
                status.value(),
                "Requisição inválida",
                ex.getMessage(),
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(error);
    }


    @ExceptionHandler({AccessDeniedException.class, AccessDeniedByBusinessException.class})
    public ResponseEntity<StandardErrorDto> handleForbiddenException(Exception ex, WebRequest request) {
        log.warn("Acesso negado para a requisição: {}", ex.getMessage());
        HttpStatus status = HttpStatus.FORBIDDEN;
        StandardErrorDto error = new StandardErrorDto(
                Instant.now(),
                status.value(),
                "Acesso negado",
                "Você não tem permissão para realizar esta ação.",
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<StandardErrorDto> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("Falha na autenticação: {}", ex.getMessage());
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        StandardErrorDto error = new StandardErrorDto(
                Instant.now(),
                status.value(),
                "Falha na autenticação",
                "Credenciais inválidas. Verifique seu email e senha.",
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class, ConstraintViolationException.class, ResourceNotFoundException.class})
    public ResponseEntity<StandardErrorDto> handleNotFoundErrors(Exception ex, WebRequest request) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardErrorDto error = new StandardErrorDto(
                Instant.now(),
                status.value(),
                "Recurso não encontrado",
                ex.getMessage(),
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardErrorDto> handleAllOtherExceptions(Exception ex, WebRequest request) {
        log.error("Ocorreu um erro inesperado: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        StandardErrorDto error = new StandardErrorDto(
                Instant.now(),
                status.value(),
                "Erro interno do servidor",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(error);
    }

    private record DadosErroValidacao(String field, String message) {
        public DadosErroValidacao(FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }
    }

    private record StandardErrorDto(Instant timestamp, Integer status, String error, String message, String path) {}

}