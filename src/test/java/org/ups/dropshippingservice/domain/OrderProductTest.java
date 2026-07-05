package org.ups.dropshippingservice.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderProductTest {

    @Test
    void constructor_validArgs_createsRecord() {
        OrderProduct p = new OrderProduct("PROD-001", "Description", 1);
        assertThat(p.productCode()).isEqualTo("PROD-001");
        assertThat(p.description()).isEqualTo("Description");
        assertThat(p.quantity()).isEqualTo(1);
    }

    @Test
    void constructor_nullProductCode_throws() {
        assertThatThrownBy(() -> new OrderProduct(null, "Description", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productCode");
    }

    @Test
    void constructor_blankProductCode_throws() {
        assertThatThrownBy(() -> new OrderProduct("  ", "Description", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productCode");
    }

    @Test
    void constructor_nullDescription_throws() {
        assertThatThrownBy(() -> new OrderProduct("PROD-001", null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("description");
    }

    @Test
    void constructor_blankDescription_throws() {
        assertThatThrownBy(() -> new OrderProduct("PROD-001", "", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("description");
    }

    @Test
    void constructor_zeroQuantity_throws() {
        assertThatThrownBy(() -> new OrderProduct("PROD-001", "Description", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity");
    }
}
