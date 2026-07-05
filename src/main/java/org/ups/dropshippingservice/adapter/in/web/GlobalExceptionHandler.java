package org.ups.dropshippingservice.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.ups.dropshippingservice.adapter.in.web.generated.model.ErrorResponse;
import org.ups.dropshippingservice.application.exception.InvalidDispatchDateException;
import org.ups.dropshippingservice.application.exception.OrderAlreadyProcessedException;
import org.ups.dropshippingservice.application.exception.OrderNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode("ORDER_NOT_FOUND");
        error.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(OrderAlreadyProcessedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyProcessed(OrderAlreadyProcessedException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode("ORDER_ALREADY_PROCESSED");
        error.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidDispatchDateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDate(InvalidDispatchDateException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode("INVALID_DISPATCH_DATE");
        error.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode("MISSING_PROVIDER_IDENTITY");
        error.setMessage("X-Provider-Id header is required");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode("REJECTION_REASON_REQUIRED");
        error.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentModification(ObjectOptimisticLockingFailureException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode("CONCURRENT_MODIFICATION");
        error.setMessage("La orden fue modificada concurrentemente; reintente la operación");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
