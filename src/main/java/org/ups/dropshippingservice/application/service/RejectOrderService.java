package org.ups.dropshippingservice.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.ups.dropshippingservice.application.exception.OrderNotFoundException;
import org.ups.dropshippingservice.application.port.in.RejectOrderUseCase;
import org.ups.dropshippingservice.application.port.out.LoadOrderPort;
import org.ups.dropshippingservice.application.port.out.NotifyRejectionPort;
import org.ups.dropshippingservice.application.port.out.SaveOrderPort;
import org.ups.dropshippingservice.domain.Order;
import org.ups.dropshippingservice.domain.event.OrderRejectedEvent;

import java.time.Instant;
import java.util.UUID;

@Service
public class RejectOrderService implements RejectOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(RejectOrderService.class);

    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;
    private final NotifyRejectionPort notifyRejectionPort;

    public RejectOrderService(LoadOrderPort loadOrderPort, SaveOrderPort saveOrderPort,
                               NotifyRejectionPort notifyRejectionPort) {
        this.loadOrderPort = loadOrderPort;
        this.saveOrderPort = saveOrderPort;
        this.notifyRejectionPort = notifyRejectionPort;
    }

    @Override
    public Order rejectOrder(UUID orderId, String providerId, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason must not be blank");
        }

        Order order = loadOrderPort.findByIdAndProviderId(orderId, providerId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.reject(rejectionReason, providerId);
        Order saved = saveOrderPort.save(order);

        OrderRejectedEvent event = new OrderRejectedEvent(
                saved.getId(),
                saved.getOrderCode(),
                saved.getCustomerContact().name(),
                saved.getProduct().productCode(),
                rejectionReason,
                Instant.now()
        );
        try {
            notifyRejectionPort.notifyRejection(event);
        } catch (RuntimeException e) {
            log.warn("[COMMERCIAL-ALERT-FAILED] Could not notify rejection for order {}: {}",
                    saved.getOrderCode(), e.getMessage());
        }

        return saved;
    }
}
