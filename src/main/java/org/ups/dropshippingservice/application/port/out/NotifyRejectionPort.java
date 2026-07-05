package org.ups.dropshippingservice.application.port.out;

import org.ups.dropshippingservice.domain.event.OrderRejectedEvent;

public interface NotifyRejectionPort {
    void notifyRejection(OrderRejectedEvent event);
}
