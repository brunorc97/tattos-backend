package com.tattos.exception;

import com.tattos.dto.response.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violação de integridade de dados em {}: {}", request.getRequestURI(), ex.getMessage());

        String detalhe = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        boolean chaveDuplicada = detalhe.contains("duplicate entry") || detalhe.contains("duplicate key");
        boolean violacaoFk = detalhe.contains("foreign key") || detalhe.contains("cannot delete or update a parent row")
                || detalhe.contains("referential integrity");

        HttpStatus status = violacaoFk ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.CONFLICT;
        String mensagem = chaveDuplicada ? msg("error.duplicate")
                : violacaoFk ? msg("error.foreign_key")
                : msg("error.data.integrity");

        ErrorResponseDTO error = ErrorResponseDTO.of(
            status.value(),
            "Conflito de Integridade de Dados",
            mensagem,
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
            errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );
        ErrorResponseDTO error = ErrorResponseDTO.of(
            HttpStatus.BAD_REQUEST.value(),
            "Parâmetro Inválido",
            msg("error.constraint.violation"),
            request.getRequestURI(),
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflict(ConflictException ex, HttpServletRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.of(
            HttpStatus.CONFLICT.value(),
            "Conflito de Agendamento ou Recurso",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusiness(BusinessException ex, HttpServletRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.of(
            HttpStatus.BAD_REQUEST.value(),
            "Regra de Negócio Violada",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.of(
            HttpStatus.NOT_FOUND.value(),
            "Recurso Não Encontrado",
            msg("error.not_found"),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponseDTO error = ErrorResponseDTO.of(
            HttpStatus.BAD_REQUEST.value(),
            "Erro de Validação de Dados",
            msg("error.validation"),
            request.getRequestURI(),
            fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ErrorResponseDTO> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.of(
            HttpStatus.UNAUTHORIZED.value(),
            "Falha de Autenticação",
            msg("error.unauthorized"),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.of(
            HttpStatus.FORBIDDEN.value(),
            "Acesso Negado",
            msg("error.forbidden"),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Erro Interno do Servidor",
            msg("error.internal"),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
