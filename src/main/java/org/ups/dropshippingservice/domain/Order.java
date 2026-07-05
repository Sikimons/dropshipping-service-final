package org.ups.dropshippingservice.domain;

import org.ups.dropshippingservice.application.exception.OrderAlreadyProcessedException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Order {

    private final UUID id;
    private final String orderCode;
    private final String providerId;
    private OrderStatus status;
    private final OrderProduct product;
    private final DeliveryAddress deliveryAddress;
    private final CustomerContact customerContact;
    private final LocalDate expectedDeliveryDate;
    private final String specialConditions;
    private LocalDate estimatedDispatchDate;
    private String rejectionReason;
    private String lastActionBy;
    private Instant lastActionAt;
    private final long version;

    public Order(UUID id, String orderCode, String providerId, OrderStatus status,
                 OrderProduct product, DeliveryAddress deliveryAddress,
                 CustomerContact customerContact, LocalDate expectedDeliveryDate,
                 String specialConditions, LocalDate estimatedDispatchDate,
                 String rejectionReason, String lastActionBy, Instant lastActionAt,
                 long version) {
        this.id = id;
        this.orderCode = orderCode;
        this.providerId = providerId;
        this.status = status;
        this.product = product;
        this.deliveryAddress = deliveryAddress;
        this.customerContact = customerContact;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.specialConditions = specialConditions;
        this.estimatedDispatchDate = estimatedDispatchDate;
        this.rejectionReason = rejectionReason;
        this.lastActionBy = lastActionBy;
        this.lastActionAt = lastActionAt;
        this.version = version;
    }

    public void accept(LocalDate dispatchDate, String actorId) {
        if (status != OrderStatus.PENDIENTE) {
            throw new OrderAlreadyProcessedException(id, status);
        }
        this.status = OrderStatus.ACEPTADO;
        this.estimatedDispatchDate = dispatchDate;
        this.lastActionBy = actorId;
        this.lastActionAt = Instant.now();
    }

    public void reject(String reason, String actorId) {
        if (status != OrderStatus.PENDIENTE) {
            throw new OrderAlreadyProcessedException(id, status);
        }
        this.status = OrderStatus.RECHAZADO;
        this.rejectionReason = reason;
        this.lastActionBy = actorId;
        this.lastActionAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getOrderCode() { return orderCode; }
    public String getProviderId() { return providerId; }
    public OrderStatus getStatus() { return status; }
    public OrderProduct getProduct() { return product; }
    public DeliveryAddress getDeliveryAddress() { return deliveryAddress; }
    public CustomerContact getCustomerContact() { return customerContact; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public String getSpecialConditions() { return specialConditions; }
    public LocalDate getEstimatedDispatchDate() { return estimatedDispatchDate; }
    public String getRejectionReason() { return rejectionReason; }
    public String getLastActionBy() { return lastActionBy; }
    public Instant getLastActionAt() { return lastActionAt; }
    public long getVersion() { return version; }
}
