package com.habitus.api.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.habitus.api.dto.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> tratarApiException(ApiException exception) {
        return montarResposta(exception.getStatus(), exception.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> tratarValidacao(MethodArgumentNotValidException exception) {
        Map<String, String> campos = new HashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            campos.put(error.getField(), error.getDefaultMessage());
        }
        return montarResposta(HttpStatus.BAD_REQUEST, "Dados da requisição inválidos", campos);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> tratarInesperado(Exception exception) {
        return montarResposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", null);
    }

    private ResponseEntity<ErrorResponse> montarResposta(HttpStatus status, String message, Map<String, String> campos) {
        ErrorResponse resposta = new ErrorResponse(
            LocalDateTime.now(),
            status.value(),
            traduzirStatus(status),
            message,
            campos
        );
        return ResponseEntity.status(status).body(resposta);
    }

    private String traduzirStatus(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Requisição inválida";
            case UNAUTHORIZED -> "Não autorizado";
            case NOT_FOUND -> "Não encontrado";
            case CONFLICT -> "Conflito";
            case INTERNAL_SERVER_ERROR -> "Erro interno do servidor";
            default -> status.getReasonPhrase();
        };
    }
}
