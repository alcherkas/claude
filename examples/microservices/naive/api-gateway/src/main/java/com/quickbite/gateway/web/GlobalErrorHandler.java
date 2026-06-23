package com.quickbite.gateway.web;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

/**
 * Global error handler for the gateway's own MVC-style endpoints (the {@code /api/gateway} and
 * {@code /internal} controllers below). Routing/proxy errors for downstream services are handled
 * reactively by Spring Cloud Gateway itself. Produces the platform-standard envelope
 * {@code {timestamp,status,error,message,path}}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex,
                                                            ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return build(status, ex.getReason() == null ? status.getReasonPhrase() : ex.getReason(),
                exchange.getRequest().getURI().getPath());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, ServerWebExchange exchange) {
        log.error("unhandled gateway error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error",
                exchange.getRequest().getURI().getPath());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return ResponseEntity.status(status).body(body);
    }
}
