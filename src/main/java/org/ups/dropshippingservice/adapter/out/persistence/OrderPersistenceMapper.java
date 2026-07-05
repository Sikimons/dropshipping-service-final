package org.ups.dropshippingservice.adapter.out.persistence;

import org.springframework.stereotype.Component;
import org.ups.dropshippingservice.domain.CustomerContact;
import org.ups.dropshippingservice.domain.DeliveryAddress;
import org.ups.dropshippingservice.domain.Order;
import org.ups.dropshippingservice.domain.OrderProduct;
import org.ups.dropshippingservice.domain.OrderStatus;

@Component
public class OrderPersistenceMapper {

    public Order toDomain(OrderJpaEntity entity) {
        OrderProduct product = new OrderProduct(
                entity.getProduct().getProductCode(),
                entity.getProduct().getProductDescription(),
                entity.getProduct().getQuantity()
        );
        DeliveryAddress address = new DeliveryAddress(
                entity.getAddress().getStreet(),
                entity.getAddress().getCity(),
                entity.getAddress().getState(),
                entity.getAddress().getPostalCode(),
                entity.getAddress().getCountry()
        );
        CustomerContact contact = new CustomerContact(
                entity.getContact().getCustomerName(),
                entity.getContact().getPhone(),
                entity.getContact().getEmail()
        );
        return new Order(
                entity.getId(),
                entity.getOrderCode(),
                entity.getProviderId(),
                OrderStatus.valueOf(entity.getStatus()),
                product,
                address,
                contact,
                entity.getExpectedDeliveryDate(),
                entity.getSpecialConditions(),
                entity.getEstimatedDispatchDate(),
                entity.getRejectionReason(),
                entity.getLastActionBy(),
                entity.getLastActionAt(),
                entity.getVersion()
        );
    }

    public OrderJpaEntity toJpaEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(order.getId());
        entity.setOrderCode(order.getOrderCode());
        entity.setProviderId(order.getProviderId());
        entity.setStatus(order.getStatus().name());
        entity.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        entity.setSpecialConditions(order.getSpecialConditions());
        entity.setEstimatedDispatchDate(order.getEstimatedDispatchDate());
        entity.setRejectionReason(order.getRejectionReason());
        entity.setLastActionBy(order.getLastActionBy());
        entity.setLastActionAt(order.getLastActionAt());

        OrderJpaEntity.ProductEmbeddable product = new OrderJpaEntity.ProductEmbeddable();
        product.setProductCode(order.getProduct().productCode());
        product.setProductDescription(order.getProduct().description());
        product.setQuantity(order.getProduct().quantity());
        entity.setProduct(product);

        OrderJpaEntity.AddressEmbeddable address = new OrderJpaEntity.AddressEmbeddable();
        address.setStreet(order.getDeliveryAddress().street());
        address.setCity(order.getDeliveryAddress().city());
        address.setState(order.getDeliveryAddress().state());
        address.setPostalCode(order.getDeliveryAddress().postalCode());
        address.setCountry(order.getDeliveryAddress().country());
        entity.setAddress(address);

        OrderJpaEntity.ContactEmbeddable contact = new OrderJpaEntity.ContactEmbeddable();
        contact.setCustomerName(order.getCustomerContact().name());
        contact.setPhone(order.getCustomerContact().phone());
        contact.setEmail(order.getCustomerContact().email());
        entity.setContact(contact);

        return entity;
    }
}
