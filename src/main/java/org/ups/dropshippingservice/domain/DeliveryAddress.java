package org.ups.dropshippingservice.domain;

public record DeliveryAddress(
        String street,
        String city,
        String state,
        String postalCode,
        String country) {

    public DeliveryAddress {
        if (street == null || street.isBlank()) throw new IllegalArgumentException("street must not be blank");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("city must not be blank");
        if (state == null || state.isBlank()) throw new IllegalArgumentException("state must not be blank");
        if (postalCode == null || postalCode.isBlank()) throw new IllegalArgumentException("postalCode must not be blank");
        if (country == null || country.isBlank()) throw new IllegalArgumentException("country must not be blank");
    }
}
