package org.ups.dropshippingservice.domain;

public record CustomerContact(String name, String phone, String email) {
    public CustomerContact {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (phone == null || phone.isBlank()) throw new IllegalArgumentException("phone must not be blank");
    }
}
