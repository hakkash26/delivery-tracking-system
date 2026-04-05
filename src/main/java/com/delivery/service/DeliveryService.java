package com.delivery.service;

import com.delivery.exception.DeliveryNotFoundException;
import com.delivery.exception.InvalidStatusTransitionException;
import com.delivery.model.Delivery;
import com.delivery.model.DeliveryStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public DeliveryService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    /**
     * Create a new delivery. Starts with PENDING status.
     */
    @Transactional
    public Delivery createDelivery(Delivery delivery) {
        if (deliveryRepository.existsByTrackingNumber(delivery.getTrackingNumber())) {
            throw new IllegalArgumentException(
                "Delivery already exists with tracking number: " + delivery.getTrackingNumber());
        }
        delivery.setStatus(DeliveryStatus.PENDING);
        return deliveryRepository.save(delivery);
    }

    /**
     * Get all deliveries.
     */
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    /**
     * Get delivery by tracking number.
     */
    public Delivery getByTrackingNumber(String trackingNumber) {
        return deliveryRepository.findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> new DeliveryNotFoundException(trackingNumber));
    }

    /**
     * Get delivery by ID.
     */
    public Delivery getById(Long id) {
        return deliveryRepository.findById(id)
            .orElseThrow(() -> new DeliveryNotFoundException(id));
    }

    /**
     * Get deliveries by status.
     */
    public List<Delivery> getByStatus(DeliveryStatus status) {
        return deliveryRepository.findByStatus(status);
    }

    /**
     * Update the status of a delivery with validation.
     */
    @Transactional
    public Delivery updateStatus(String trackingNumber, DeliveryStatus newStatus, String remarks) {
        Delivery delivery = getByTrackingNumber(trackingNumber);

        DeliveryStatus currentStatus = delivery.getStatus();
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(currentStatus.name(), newStatus.name());
        }

        delivery.setStatus(newStatus);
        if (remarks != null && !remarks.isBlank()) {
            delivery.setRemarks(remarks);
        }

        return deliveryRepository.save(delivery);
    }

    /**
     * Delete a delivery (only allowed if PENDING or CANCELLED).
     */
    @Transactional
    public void deleteDelivery(String trackingNumber) {
        Delivery delivery = getByTrackingNumber(trackingNumber);
        if (delivery.getStatus() != DeliveryStatus.PENDING
                && delivery.getStatus() != DeliveryStatus.CANCELLED) {
            throw new IllegalStateException("Cannot delete an active delivery. Cancel it first.");
        }
        deliveryRepository.delete(delivery);
    }
}
