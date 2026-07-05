package org.ups.dropshippingservice.adapter.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.ups.dropshippingservice.adapter.in.web.GlobalExceptionHandler;
import org.ups.dropshippingservice.adapter.in.web.OrderController;
import org.ups.dropshippingservice.adapter.in.web.OrderControllerMapper;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.ups.dropshippingservice.application.exception.InvalidDispatchDateException;
import org.ups.dropshippingservice.application.exception.OrderAlreadyProcessedException;
import org.ups.dropshippingservice.application.exception.OrderNotFoundException;
import org.ups.dropshippingservice.application.port.in.AcceptOrderUseCase;
import org.ups.dropshippingservice.application.port.in.GetAssignedOrdersUseCase;
import org.ups.dropshippingservice.application.port.in.RejectOrderUseCase;
import org.ups.dropshippingservice.domain.CustomerContact;
import org.ups.dropshippingservice.domain.DeliveryAddress;
import org.ups.dropshippingservice.domain.Order;
import org.ups.dropshippingservice.domain.OrderProduct;
import org.ups.dropshippingservice.domain.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({OrderController.class, OrderControllerMapper.class, GlobalExceptionHandler.class})
class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAssignedOrdersUseCase getAssignedOrdersUseCase;
    @MockitoBean
    private AcceptOrderUseCase acceptOrderUseCase;
    @MockitoBean
    private RejectOrderUseCase rejectOrderUseCase;

    @Test
    void getAssignedOrders_returnsOrders() throws Exception {
        Order order = buildOrder(UUID.randomUUID(), "prov-001", OrderStatus.PENDIENTE);
        when(getAssignedOrdersUseCase.getAssignedOrders("prov-001")).thenReturn(List.of(order));

        mockMvc.perform(get("/api/v1/providers/prov-001/orders")
                        .header("X-Provider-Id", "prov-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderCode").value("ORD-001"));
    }

    @Test
    void getAssignedOrders_withMismatchedHeader_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/providers/prov-002/orders")
                        .header("X-Provider-Id", "prov-001"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAssignedOrders_withUnknownProvider_returnsEmptyArray() throws Exception {
        when(getAssignedOrdersUseCase.getAssignedOrders("prov-999")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/providers/prov-999/orders")
                        .header("X-Provider-Id", "prov-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void acceptOrder_happyPath_returns200() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, "prov-001", OrderStatus.ACEPTADO);
        when(acceptOrderUseCase.acceptOrder(eq(orderId), eq("prov-001"), any(LocalDate.class))).thenReturn(order);

        mockMvc.perform(put("/api/v1/orders/" + orderId + "/accept")
                        .header("X-Provider-Id", "prov-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estimatedDispatchDate\":\"2026-12-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACEPTADO"));
    }

    @Test
    void acceptOrder_withPastDate_returns400() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(acceptOrderUseCase.acceptOrder(eq(orderId), eq("prov-001"), any(LocalDate.class)))
                .thenThrow(new InvalidDispatchDateException(LocalDate.now().minusDays(1)));

        mockMvc.perform(put("/api/v1/orders/" + orderId + "/accept")
                        .header("X-Provider-Id", "prov-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estimatedDispatchDate\":\"2020-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DISPATCH_DATE"));
    }

    @Test
    void acceptOrder_alreadyProcessed_returns409() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(acceptOrderUseCase.acceptOrder(eq(orderId), eq("prov-001"), any(LocalDate.class)))
                .thenThrow(new OrderAlreadyProcessedException(orderId, OrderStatus.ACEPTADO));

        mockMvc.perform(put("/api/v1/orders/" + orderId + "/accept")
                        .header("X-Provider-Id", "prov-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estimatedDispatchDate\":\"2026-12-01\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_ALREADY_PROCESSED"));
    }

    @Test
    void rejectOrder_happyPath_returns200() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, "prov-001", OrderStatus.RECHAZADO);
        when(rejectOrderUseCase.rejectOrder(eq(orderId), eq("prov-001"), eq("No stock"))).thenReturn(order);

        mockMvc.perform(put("/api/v1/orders/" + orderId + "/reject")
                        .header("X-Provider-Id", "prov-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rejectionReason\":\"No stock\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECHAZADO"));
    }

    @Test
    void rejectOrder_blankReason_returns400() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(rejectOrderUseCase.rejectOrder(eq(orderId), eq("prov-001"), eq("")))
                .thenThrow(new IllegalArgumentException("Rejection reason must not be blank"));

        mockMvc.perform(put("/api/v1/orders/" + orderId + "/reject")
                        .header("X-Provider-Id", "prov-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rejectionReason\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REJECTION_REASON_REQUIRED"));
    }

    @Test
    void rejectOrder_alreadyProcessed_returns409() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(rejectOrderUseCase.rejectOrder(eq(orderId), eq("prov-001"), any()))
                .thenThrow(new OrderAlreadyProcessedException(orderId, OrderStatus.ACEPTADO));

        mockMvc.perform(put("/api/v1/orders/" + orderId + "/reject")
                        .header("X-Provider-Id", "prov-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rejectionReason\":\"reason\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_ALREADY_PROCESSED"));
    }

    @Test
    void acceptOrder_orderNotFound_returns404() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(acceptOrderUseCase.acceptOrder(eq(orderId), eq("prov-001"), any()))
                .thenThrow(new OrderNotFoundException(orderId));

        mockMvc.perform(put("/api/v1/orders/" + orderId + "/accept")
                        .header("X-Provider-Id", "prov-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estimatedDispatchDate\":\"2026-12-01\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }

    @Test
    void acceptOrder_whenConcurrentModification_returns409() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(acceptOrderUseCase.acceptOrder(eq(orderId), eq("prov-001"), any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Order.class, orderId));

        mockMvc.perform(put("/api/v1/orders/" + orderId + "/accept")
                        .header("X-Provider-Id", "prov-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estimatedDispatchDate\":\"2026-12-01\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONCURRENT_MODIFICATION"));
    }

    private Order buildOrder(UUID id, String providerId, OrderStatus status) {
        return new Order(id, "ORD-001", providerId, status,
                new OrderProduct("P001", "Product", 1),
                new DeliveryAddress("St 1", "City", "State", "00001", "Country"),
                new CustomerContact("Client", "+1", null),
                LocalDate.now().plusDays(10), null, null, null, null, null, 0L);
    }
}
