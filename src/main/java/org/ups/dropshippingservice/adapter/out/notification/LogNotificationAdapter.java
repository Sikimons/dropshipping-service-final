package org.ups.dropshippingservice.adapter.out.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.ups.dropshippingservice.application.port.out.NotifyRejectionPort;
import org.ups.dropshippingservice.domain.event.OrderRejectedEvent;

@Component
public class LogNotificationAdapter implements NotifyRejectionPort {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationAdapter.class);

    @Override
    public void notifyRejection(OrderRejectedEvent event) {
        log.info("[COMMERCIAL-ALERT] Order {} rejected. Customer: {}. Product: {}. Reason: {}",
                event.orderCode(),
                event.customerName(),
                event.productCode(),
                event.rejectionReason());
    }
}
