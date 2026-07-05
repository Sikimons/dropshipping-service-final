package org.ups.dropshippingservice.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.dropshippingservice.application.exception.OrderAlreadyProcessedException;
import org.ups.dropshippingservice.application.exception.OrderNotFoundException;
import org.ups.dropshippingservice.application.port.out.LoadOrderPort;
import org.ups.dropshippingservice.application.port.out.NotifyRejectionPort;
import org.ups.dropshippingservice.application.port.out.SaveOrderPort;
import org.ups.dropshippingservice.application.service.RejectOrderService;
import org.ups.dropshippingservice.domain.CustomerContact;
import org.ups.dropshippingservice.domain.DeliveryAddress;
import org.ups.dropshippingservice.domain.Order;
import org.ups.dropshippingservice.domain.OrderProduct;
import org.ups.dropshippingservice.domain.OrderStatus;
import org.ups.dropshippingservice.domain.event.OrderRejectedEvent;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RejectOrderServiceTest {

    @Mock
    private LoadOrderPort loadOrderPort;
    @Mock
    private SaveOrderPort saveOrderPort;
    @Mock
    private NotifyRejectionPort notifyRejectionPort;

    private RejectOrderService service;

    @BeforeEach
    void setUp() {
        service = new RejectOrderService(loadOrderPort, saveOrderPort, notifyRejectionPort);
    }

    @Test
    void rejectOrder_happyPath_savesAndNotifies() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, "prov-001", OrderStatus.PENDIENTE);
        when(loadOrderPort.findByIdAndProviderId(orderId, "prov-001")).thenReturn(Optional.of(order));
        when(saveOrderPort.save(any())).thenReturn(order);

        service.rejectOrder(orderId, "prov-001", "No stock");

        verify(saveOrderPort).save(order);
        ArgumentCaptor<OrderRejectedEvent> captor = ArgumentCaptor.forClass(OrderRejectedEvent.class);
        verify(notifyRejectionPort).notifyRejection(captor.capture());
        assertThat(captor.getValue().rejectionReason()).isEqualTo("No stock");
    }

    @Test
    void rejectOrder_alreadyProcessed_throwsAlreadyProcessed() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, "prov-001", OrderStatus.RECHAZADO);
        when(loadOrderPort.findByIdAndProviderId(orderId, "prov-001")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.rejectOrder(orderId, "prov-001", "reason"))
                .isInstanceOf(OrderAlreadyProcessedException.class);
    }

    @Test
    void rejectOrder_blankReason_throwsIllegalArgument() {
        UUID orderId = UUID.randomUUID();

        assertThatThrownBy(() -> service.rejectOrder(orderId, "prov-001", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectOrder_orderNotFound_throwsNotFound() {
        UUID orderId = UUID.randomUUID();
        when(loadOrderPort.findByIdAndProviderId(orderId, "prov-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rejectOrder(orderId, "prov-001", "reason"))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void rejectOrder_whenNotificationFails_rejectionStillPersists() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, "prov-001", OrderStatus.PENDIENTE);
        when(loadOrderPort.findByIdAndProviderId(orderId, "prov-001")).thenReturn(Optional.of(order));
        when(saveOrderPort.save(any())).thenReturn(order);
        doThrow(new RuntimeException("Notification service unavailable"))
                .when(notifyRejectionPort).notifyRejection(any());

        assertThatCode(() -> service.rejectOrder(orderId, "prov-001", "No stock"))
                .doesNotThrowAnyException();

        verify(saveOrderPort).save(order);
    }

    private Order buildOrder(UUID id, String providerId, OrderStatus status) {
        return new Order(id, "ORD-001", providerId, status,
                new OrderProduct("P001", "Product", 1),
                new DeliveryAddress("St 1", "City", "State", "00001", "Country"),
                new CustomerContact("Client", "+1", null),
                LocalDate.now().plusDays(10), null, null, null, null, null, 0L);
    }
}
