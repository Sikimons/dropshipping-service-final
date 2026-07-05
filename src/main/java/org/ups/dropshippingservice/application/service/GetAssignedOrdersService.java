package org.ups.dropshippingservice.application.service;

import org.springframework.stereotype.Service;
import org.ups.dropshippingservice.application.port.in.GetAssignedOrdersUseCase;
import org.ups.dropshippingservice.application.port.out.LoadOrderPort;
import org.ups.dropshippingservice.domain.Order;

import java.util.List;

@Service
public class GetAssignedOrdersService implements GetAssignedOrdersUseCase {

    private final LoadOrderPort loadOrderPort;

    public GetAssignedOrdersService(LoadOrderPort loadOrderPort) {
        this.loadOrderPort = loadOrderPort;
    }

    @Override
    public List<Order> getAssignedOrders(String providerId) {
        return loadOrderPort.findAllByProviderId(providerId);
    }
}
