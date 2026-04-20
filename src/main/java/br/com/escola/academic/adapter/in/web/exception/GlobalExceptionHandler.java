package br.com.escola.academic.adapter.in.web.exception;

import br.com.escola.academic.domain.exception.BusinessException;
import br.com.escola.academic.domain.exception.TransientException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================
    // ERRO DE NEGÓCIO
    // =========================================
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {

        ErrorResponse response = new ErrorResponse(
                ex.getMessage(),
                "BUSINESS_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // =========================================
    // ERRO TRANSIENTE
    // =========================================
    @ExceptionHandler(TransientException.class)
    public ResponseEntity<ErrorResponse> handleTransient(TransientException ex) {

        ErrorResponse response = new ErrorResponse(
                ex.getMessage(),
                "TRANSIENT_ERROR",
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    // =========================================
    // ERRO GENÉRICO
    // =========================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {

        ErrorResponse response = new ErrorResponse(
                "Erro interno inesperado",
                ex.getClass().getSimpleName(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}