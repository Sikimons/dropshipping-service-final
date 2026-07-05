package org.ups.dropshippingservice.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.dropshippingservice.application.port.out.LoadOrderPort;
import org.ups.dropshippingservice.application.service.GetAssignedOrdersService;
import org.ups.dropshippingservice.domain.CustomerContact;
import org.ups.dropshippingservice.domain.DeliveryAddress;
import org.ups.dropshippingservice.domain.Order;
import org.ups.dropshippingservice.domain.OrderProduct;
import org.ups.dropshippingservice.domain.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAssignedOrdersServiceTest {

    @Mock
    private LoadOrderPort loadOrderPort;

    private GetAssignedOrdersService service;

    @BeforeEach
    void setUp() {
        service = new GetAssignedOrdersService(loadOrderPort);
    }

    @Test
    void getAssignedOrders_whenOrdersExist_returnsOrders() {
        Order order = buildOrder("prov-001");
        when(loadOrderPort.findAllByProviderId("prov-001")).thenReturn(List.of(order));

        List<Order> result = service.getAssignedOrders("prov-001");

        assertThat(result).hasSize(1);
        verify(loadOrderPort).findAllByProviderId("prov-001");
    }

    @Test
    void getAssignedOrders_whenNoOrders_returnsEmptyList() {
        when(loadOrderPort.findAllByProviderId("prov-999")).thenReturn(List.of());

        List<Order> result = service.getAssignedOrders("prov-999");

        assertThat(result).isEmpty();
        verify(loadOrderPort).findAllByProviderId("prov-999");
    }

    private Order buildOrder(String providerId) {
        return new Order(UUID.randomUUID(), "ORD-001", providerId, OrderStatus.PENDIENTE,
                new OrderProduct("P001", "Product", 1),
                new DeliveryAddress("St 1", "City", "State", "00001", "Country"),
                new CustomerContact("Client", "+1", null),
                LocalDate.now().plusDays(10), null, null, null, null, null, 0L);
    }
}
