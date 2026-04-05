package com.delivery.service;

import com.delivery.model.Delivery;
import com.delivery.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByTrackingNumber(String trackingNumber);
    List<Delivery> findByStatus(DeliveryStatus status);
    boolean existsByTrackingNumber(String trackingNumber);
}
