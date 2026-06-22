package com.example.el_encashment.controller;

import com.example.el_encashment.model.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
        ResponseStatusException ex,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() == null || ex.getReason().isBlank()
            ? status.getReasonPhrase()
            : ex.getReason();
        return ResponseEntity.status(status).body(
            new ApiErrorResponse(status.value(), status.getReasonPhrase(), message, request.getRequestURI())
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
        DataIntegrityViolationException ex,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = resolveDataIntegrityMessage(ex);
        return ResponseEntity.status(status).body(
            new ApiErrorResponse(status.value(), status.getReasonPhrase(), message, request.getRequestURI())
        );
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequestExceptions(
        Exception ex,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = resolveBadRequestMessage(ex);
        return ResponseEntity.status(status).body(
            new ApiErrorResponse(status.value(), status.getReasonPhrase(), message, request.getRequestURI())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(
            new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Unexpected server error. Please try again.",
                request.getRequestURI()
            )
        );
    }

    private String resolveDataIntegrityMessage(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() == null ? ex.getMessage() : ex.getMostSpecificCause().getMessage();
        if (message == null || message.isBlank()) {
            return "Unable to save record because of database validation.";
        }
        if (message.contains("CGEIS_BILL_ITEM") && message.contains("VALUE_AMOUNT")) {
            return "Value is required for each selected CGEIS row.";
        }
        if (message.contains("CGEIS_BILL_ITEM") && message.contains("TIMES")) {
            return "Times is required for each selected CGEIS row.";
        }
        if (message.contains("CGEIS_BILL") && message.contains("BILL_NO")) {
            return "Bill No is required and must be valid.";
        }
        return "Unable to save record because of database validation.";
    }

    private String resolveBadRequestMessage(Exception ex) {
        if (ex instanceof MissingServletRequestParameterException missingParam) {
            return missingParam.getParameterName() + " is required.";
        }
        if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            return mismatch.getName() + " has an invalid value.";
        }
        if (ex instanceof HttpMessageNotReadableException) {
            return "Request body is invalid or contains malformed values.";
        }
        return "Request is invalid.";
    }
}
