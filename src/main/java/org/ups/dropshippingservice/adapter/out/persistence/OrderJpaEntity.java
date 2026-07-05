package org.ups.dropshippingservice.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderJpaEntity {

    @Id
    private UUID id;

    @Column(name = "order_code", nullable = false, unique = true)
    private String orderCode;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "status", nullable = false)
    private String status;

    @Embedded
    private ProductEmbeddable product;

    @Embedded
    private AddressEmbeddable address;

    @Embedded
    private ContactEmbeddable contact;

    @Column(name = "expected_delivery_date", nullable = false)
    private LocalDate expectedDeliveryDate;

    @Column(name = "special_conditions")
    private String specialConditions;

    @Column(name = "estimated_dispatch_date")
    private LocalDate estimatedDispatchDate;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "last_action_by")
    private String lastActionBy;

    @Column(name = "last_action_at")
    private Instant lastActionAt;

    @Version
    private long version;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ProductEmbeddable {
        @Column(name = "product_code", nullable = false)
        private String productCode;

        @Column(name = "product_description", nullable = false)
        private String productDescription;

        @Column(name = "quantity", nullable = false)
        private int quantity;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class AddressEmbeddable {
        @Column(name = "street", nullable = false)
        private String street;

        @Column(name = "city", nullable = false)
        private String city;

        @Column(name = "state", nullable = false)
        private String state;

        @Column(name = "postal_code", nullable = false)
        private String postalCode;

        @Column(name = "country", nullable = false)
        private String country;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ContactEmbeddable {
        @Column(name = "customer_name", nullable = false)
        private String customerName;

        @Column(name = "phone", nullable = false)
        private String phone;

        @Column(name = "email")
        private String email;
    }
}
