package org.ups.dropshippingservice.application.port.in;

import org.ups.dropshippingservice.domain.Order;

import java.util.List;

public interface GetAssignedOrdersUseCase {
    List<Order> getAssignedOrders(String providerId);
}
