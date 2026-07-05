package org.ups.dropshippingservice.application.port.out;

import org.ups.dropshippingservice.domain.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadOrderPort {
    Optional<Order> findByIdAndProviderId(UUID orderId, String providerId);
    List<Order> findAllByProviderId(String providerId);
}
