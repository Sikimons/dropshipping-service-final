package org.ups.dropshippingservice.adapter.out.persistence;

import org.springframework.stereotype.Component;
import org.ups.dropshippingservice.application.port.out.LoadOrderPort;
import org.ups.dropshippingservice.application.port.out.SaveOrderPort;
import org.ups.dropshippingservice.domain.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderPersistenceAdapter implements LoadOrderPort, SaveOrderPort {

    private final SpringDataOrderRepository repository;
    private final OrderPersistenceMapper mapper;

    public OrderPersistenceAdapter(SpringDataOrderRepository repository, OrderPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Order> findByIdAndProviderId(UUID orderId, String providerId) {
        return repository.findByIdAndProviderId(orderId, providerId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Order> findAllByProviderId(String providerId) {
        return repository.findAllByProviderId(providerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = mapper.toJpaEntity(order);
        OrderJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
