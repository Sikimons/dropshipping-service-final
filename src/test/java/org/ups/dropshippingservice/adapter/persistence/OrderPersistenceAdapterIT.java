package org.ups.dropshippingservice.adapter.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.ups.dropshippingservice.adapter.out.persistence.OrderPersistenceAdapter;
import org.ups.dropshippingservice.adapter.out.persistence.OrderPersistenceMapper;
import org.ups.dropshippingservice.adapter.out.persistence.SpringDataOrderRepository;
import org.ups.dropshippingservice.domain.Order;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({OrderPersistenceAdapter.class, OrderPersistenceMapper.class})
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Sql(scripts = {"classpath:db/schema.sql", "classpath:db/data.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OrderPersistenceAdapterIT {

    @Autowired
    private OrderPersistenceAdapter adapter;

    @Autowired
    private SpringDataOrderRepository repository;

    private static final UUID ORDER_1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID ORDER_2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

    @Test
    void findByIdAndProviderId_returnsSeededOrder() {
        Optional<Order> result = adapter.findByIdAndProviderId(ORDER_1, "prov-001");
        assertThat(result).isPresent();
        assertThat(result.get().getProviderId()).isEqualTo("prov-001");
    }

    @Test
    void findAllByProviderId_returnsBothProv001Orders() {
        List<Order> orders = adapter.findAllByProviderId("prov-001");
        assertThat(orders).hasSize(2);
    }

    @Test
    void findAllByProviderId_withUnknownProvider_returnsEmpty() {
        List<Order> orders = adapter.findAllByProviderId("prov-999");
        assertThat(orders).isEmpty();
    }

    @Test
    void save_persistsOrderChanges() {
        Order order = adapter.findByIdAndProviderId(ORDER_2, "prov-001").orElseThrow();
        order.accept(LocalDate.now().plusDays(5), "prov-001");
        Order saved = adapter.save(order);
        assertThat(saved.getStatus().name()).isEqualTo("ACEPTADO");
    }
}
