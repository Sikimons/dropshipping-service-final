package org.ups.dropshippingservice.application.port.in;

import org.ups.dropshippingservice.domain.Order;

import java.util.UUID;

public interface RejectOrderUseCase {
    Order rejectOrder(UUID orderId, String providerId, String rejectionReason);
}
