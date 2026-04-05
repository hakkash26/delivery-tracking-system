package com.delivery.exception;

public class DeliveryNotFoundException extends RuntimeException {
    public DeliveryNotFoundException(String trackingNumber) {
        super("Delivery not found with tracking number: " + trackingNumber);
    }
    public DeliveryNotFoundException(Long id) {
        super("Delivery not found with id: " + id);
    }
}
