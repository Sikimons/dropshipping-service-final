package org.ups.dropshippingservice.application.port.in;

import org.ups.dropshippingservice.domain.Order;

import java.time.LocalDate;
import java.util.UUID;

public interface AcceptOrderUseCase {
    Order acceptOrder(UUID orderId, String providerId, LocalDate estimatedDispatchDate);
}
