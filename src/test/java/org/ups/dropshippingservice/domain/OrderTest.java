package org.ups.dropshippingservice.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ups.dropshippingservice.application.exception.OrderAlreadyProcessedException;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order(
                UUID.randomUUID(), "ORD-001", "prov-001", OrderStatus.PENDIENTE,
                new OrderProduct("PROD-001", "Test product", 1),
                new DeliveryAddress("Street 1", "City", "State", "00001", "Country"),
                new CustomerContact("Client Name", "+1234567890", null),
                LocalDate.now().plusDays(10), null, null, null, null, null, 0L
        );
    }

    @Test
    void accept_shouldTransitionStatusToAceptado() {
        LocalDate dispatchDate = LocalDate.now().plusDays(3);
        order.accept(dispatchDate, "prov-001");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.ACEPTADO);
        assertThat(order.getEstimatedDispatchDate()).isEqualTo(dispatchDate);
        assertThat(order.getLastActionBy()).isEqualTo("prov-001");
        assertThat(order.getLastActionAt()).isNotNull();
    }

    @Test
    void accept_onAceptadoOrder_shouldThrow() {
        order.accept(LocalDate.now().plusDays(1), "prov-001");
        assertThatThrownBy(() -> order.accept(LocalDate.now().plusDays(2), "prov-001"))
                .isInstanceOf(OrderAlreadyProcessedException.class);
    }

    @Test
    void reject_shouldTransitionStatusToRechazado() {
        order.reject("No stock", "prov-001");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.RECHAZADO);
        assertThat(order.getRejectionReason()).isEqualTo("No stock");
        assertThat(order.getLastActionBy()).isEqualTo("prov-001");
        assertThat(order.getLastActionAt()).isNotNull();
    }

    @Test
    void reject_onRechazadoOrder_shouldThrow() {
        order.reject("No stock", "prov-001");
        assertThatThrownBy(() -> order.reject("Another reason", "prov-001"))
                .isInstanceOf(OrderAlreadyProcessedException.class);
    }

    @Test
    void getVersion_returnsInitialVersion() {
        assertThat(order.getVersion()).isEqualTo(0L);
    }
}
