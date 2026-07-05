package org.ups.dropshippingservice.application.service;

import org.springframework.stereotype.Service;
import org.ups.dropshippingservice.application.exception.InvalidDispatchDateException;
import org.ups.dropshippingservice.application.exception.OrderNotFoundException;
import org.ups.dropshippingservice.application.port.in.AcceptOrderUseCase;
import org.ups.dropshippingservice.application.port.out.LoadOrderPort;
import org.ups.dropshippingservice.application.port.out.SaveOrderPort;
import org.ups.dropshippingservice.domain.Order;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AcceptOrderService implements AcceptOrderUseCase {

    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;

    public AcceptOrderService(LoadOrderPort loadOrderPort, SaveOrderPort saveOrderPort) {
        this.loadOrderPort = loadOrderPort;
        this.saveOrderPort = saveOrderPort;
    }

    @Override
    public Order acceptOrder(UUID orderId, String providerId, LocalDate estimatedDispatchDate) {
        Order order = loadOrderPort.findByIdAndProviderId(orderId, providerId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (estimatedDispatchDate.isBefore(LocalDate.now())) {
            throw new InvalidDispatchDateException(estimatedDispatchDate);
        }

        order.accept(estimatedDispatchDate, providerId);
        return saveOrderPort.save(order);
    }
}
