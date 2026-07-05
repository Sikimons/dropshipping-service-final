package org.ups.dropshippingservice.application.exception;

import java.time.LocalDate;

public class InvalidDispatchDateException extends RuntimeException {
    public InvalidDispatchDateException(LocalDate date) {
        super("Dispatch date must be today or in the future: " + date);
    }
}
