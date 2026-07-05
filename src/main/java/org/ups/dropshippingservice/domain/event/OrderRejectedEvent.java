package org.ups.dropshippingservice.domain.event;

import java.time.Instant;
import java.util.UUID;

public record OrderRejectedEvent(
        UUID orderId,
        String orderCode,
        String customerName,
        String productCode,
        String rejectionReason,
        Instant rejectedAt) {
}
