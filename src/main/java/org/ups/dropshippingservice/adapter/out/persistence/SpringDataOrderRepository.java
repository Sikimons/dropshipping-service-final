package org.ups.dropshippingservice.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {
    Optional<OrderJpaEntity> findByIdAndProviderId(UUID id, String providerId);
    List<OrderJpaEntity> findAllByProviderId(String providerId);
}
