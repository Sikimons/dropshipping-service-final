package org.ups.dropshippingservice.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.dropshippingservice.application.exception.InvalidDispatchDateException;
import org.ups.dropshippingservice.application.exception.OrderAlreadyProcessedException;
import org.ups.dropshippingservice.application.exception.OrderNotFoundException;
import org.ups.dropshippingservice.application.port.out.LoadOrderPort;
import org.ups.dropshippingservice.application.port.out.SaveOrderPort;
import org.ups.dropshippingservice.application.service.AcceptOrderService;
import org.ups.dropshippingservice.domain.CustomerContact;
import org.ups.dropshippingservice.domain.DeliveryAddress;
import org.ups.dropshippingservice.domain.Order;
import org.ups.dropshippingservice.domain.OrderProduct;
import org.ups.dropshippingservice.domain.OrderStatus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcceptOrderServiceTest {

    @Mock
    private LoadOrderPort loadOrderPort;
    @Mock
    private SaveOrderPort saveOrderPort;

    private AcceptOrderService service;

    @BeforeEach
    void setUp() {
        service = new AcceptOrderService(loadOrderPort, saveOrderPort);
    }

    @Test
    void acceptOrder_happyPath_savesOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, "prov-001", OrderStatus.PENDIENTE);
        when(loadOrderPort.findByIdAndProviderId(orderId, "prov-001")).thenReturn(Optional.of(order));
        when(saveOrderPort.save(any())).thenReturn(order);

        service.acceptOrder(orderId, "prov-001", LocalDate.now().plusDays(3));

        verify(saveOrderPort).save(order);
    }

    @Test
    void acceptOrder_withPastDate_throwsInvalidDispatchDate() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, "prov-001", OrderStatus.PENDIENTE);
        when(loadOrderPort.findByIdAndProviderId(orderId, "prov-001")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.acceptOrder(orderId, "prov-001", LocalDate.now().minusDays(1)))
                .isInstanceOf(InvalidDispatchDateException.class);
    }

    @Test
    void acceptOrder_alreadyAccepted_throwsAlreadyProcessed() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, "prov-001", OrderStatus.ACEPTADO);
        when(loadOrderPort.findByIdAndProviderId(orderId, "prov-001")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.acceptOrder(orderId, "prov-001", LocalDate.now().plusDays(1)))
                .isInstanceOf(OrderAlreadyProcessedException.class);
    }

    @Test
    void acceptOrder_orderNotFound_throwsNotFound() {
        UUID orderId = UUID.randomUUID();
        when(loadOrderPort.findByIdAndProviderId(orderId, "prov-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.acceptOrder(orderId, "prov-001", LocalDate.now().plusDays(1)))
                .isInstanceOf(OrderNotFoundException.class);
    }

    private Order buildOrder(UUID id, String providerId, OrderStatus status) {
        return new Order(id, "ORD-001", providerId, status,
                new OrderProduct("P001", "Product", 1),
                new DeliveryAddress("St 1", "City", "State", "00001", "Country"),
                new CustomerContact("Client", "+1", null),
                LocalDate.now().plusDays(10), null, null, null, null, null, 0L);
    }
}
