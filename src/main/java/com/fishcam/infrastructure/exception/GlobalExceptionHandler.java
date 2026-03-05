package com.fishcam.infrastructure.exception;

import com.fishcam.adapter.web.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Ressource non trouvée: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .code(404)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.error("Erreur métier: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .code(400)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Erreur de validation");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .code(400)
                        .message("Données invalides")
                        .fieldErrors(errors)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Erreur inattendue", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .code(500)
                        .message("Une erreur est survenue")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Argument invalide: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .code(400)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // Ajouter ce handler dans GlobalExceptionHandler
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Accès refusé: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .code(403)
                        .message("Accès refusé : " + ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}