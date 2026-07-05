package org.ups.dropshippingservice.application.port.out;

import org.ups.dropshippingservice.domain.Order;

public interface SaveOrderPort {
    Order save(Order order);
}
