package org.ups.dropshippingservice.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ups.dropshippingservice.adapter.in.web.generated.ProviderOrdersApi;
import org.ups.dropshippingservice.adapter.in.web.generated.model.AcceptOrderRequest;
import org.ups.dropshippingservice.adapter.in.web.generated.model.ErrorResponse;
import org.ups.dropshippingservice.adapter.in.web.generated.model.OrderResponse;
import org.ups.dropshippingservice.adapter.in.web.generated.model.RejectOrderRequest;
import org.ups.dropshippingservice.application.port.in.AcceptOrderUseCase;
import org.ups.dropshippingservice.application.port.in.GetAssignedOrdersUseCase;
import org.ups.dropshippingservice.application.port.in.RejectOrderUseCase;
import org.ups.dropshippingservice.domain.Order;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class OrderController implements ProviderOrdersApi {

    private final GetAssignedOrdersUseCase getAssignedOrdersUseCase;
    private final AcceptOrderUseCase acceptOrderUseCase;
    private final RejectOrderUseCase rejectOrderUseCase;
    private final OrderControllerMapper mapper;

    public OrderController(GetAssignedOrdersUseCase getAssignedOrdersUseCase,
                           AcceptOrderUseCase acceptOrderUseCase,
                           RejectOrderUseCase rejectOrderUseCase,
                           OrderControllerMapper mapper) {
        this.getAssignedOrdersUseCase = getAssignedOrdersUseCase;
        this.acceptOrderUseCase = acceptOrderUseCase;
        this.rejectOrderUseCase = rejectOrderUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<List<OrderResponse>> getAssignedOrders(String providerId, String xProviderId) {
        if (!xProviderId.equals(providerId)) {
            ErrorResponse error = new ErrorResponse();
            error.setCode("ACCESS_DENIED");
            error.setMessage("Access denied");
            return ResponseEntity.status(403).build();
        }
        List<OrderResponse> responses = getAssignedOrdersUseCase.getAssignedOrders(providerId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<OrderResponse> acceptOrder(UUID orderId, String xProviderId,
                                                      AcceptOrderRequest acceptOrderRequest) {
        Order order = acceptOrderUseCase.acceptOrder(orderId, xProviderId,
                acceptOrderRequest.getEstimatedDispatchDate());
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @Override
    public ResponseEntity<OrderResponse> rejectOrder(UUID orderId, String xProviderId,
                                                      RejectOrderRequest rejectOrderRequest) {
        Order order = rejectOrderUseCase.rejectOrder(orderId, xProviderId,
                rejectOrderRequest.getRejectionReason());
        return ResponseEntity.ok(mapper.toResponse(order));
    }
}
