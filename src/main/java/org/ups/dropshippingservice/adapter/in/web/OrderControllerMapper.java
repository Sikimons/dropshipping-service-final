package org.ups.dropshippingservice.adapter.in.web;

import org.springframework.stereotype.Component;
import org.ups.dropshippingservice.adapter.in.web.generated.model.CustomerContact;
import org.ups.dropshippingservice.adapter.in.web.generated.model.DeliveryAddress;
import org.ups.dropshippingservice.adapter.in.web.generated.model.OrderResponse;
import org.ups.dropshippingservice.adapter.in.web.generated.model.OrderStatus;
import org.ups.dropshippingservice.adapter.in.web.generated.model.ProductDetails;
import org.ups.dropshippingservice.domain.Order;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class OrderControllerMapper {

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderCode(order.getOrderCode());
        response.setStatus(OrderStatus.valueOf(order.getStatus().name()));

        ProductDetails product = new ProductDetails();
        product.setProductCode(order.getProduct().productCode());
        product.setDescription(order.getProduct().description());
        product.setQuantity(order.getProduct().quantity());
        response.setProduct(product);

        DeliveryAddress address = new DeliveryAddress();
        address.setStreet(order.getDeliveryAddress().street());
        address.setCity(order.getDeliveryAddress().city());
        address.setState(order.getDeliveryAddress().state());
        address.setPostalCode(order.getDeliveryAddress().postalCode());
        address.setCountry(order.getDeliveryAddress().country());
        response.setDeliveryAddress(address);

        CustomerContact contact = new CustomerContact();
        contact.setName(order.getCustomerContact().name());
        contact.setPhone(order.getCustomerContact().phone());
        contact.setEmail(order.getCustomerContact().email());
        response.setCustomerContact(contact);

        response.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        response.setSpecialConditions(order.getSpecialConditions());
        response.setEstimatedDispatchDate(order.getEstimatedDispatchDate());
        response.setRejectionReason(order.getRejectionReason());
        response.setLastActionBy(order.getLastActionBy());

        if (order.getLastActionAt() != null) {
            response.setLastActionAt(OffsetDateTime.ofInstant(order.getLastActionAt(), ZoneOffset.UTC));
        }

        return response;
    }
}
