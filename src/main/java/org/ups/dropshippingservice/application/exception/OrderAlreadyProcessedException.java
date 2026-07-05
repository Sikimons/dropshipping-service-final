package org.ups.dropshippingservice.application.exception;

import org.ups.dropshippingservice.domain.OrderStatus;

import java.util.UUID;

public class OrderAlreadyProcessedException extends RuntimeException {
    public OrderAlreadyProcessedException(UUID orderId, OrderStatus currentStatus) {
        super("Order " + orderId + " is already " + currentStatus);
    }
}
