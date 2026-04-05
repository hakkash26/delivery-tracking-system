package com.delivery.controller;

import com.delivery.exception.DeliveryNotFoundException;
import com.delivery.exception.InvalidStatusTransitionException;
import com.delivery.model.Delivery;
import com.delivery.model.DeliveryStatus;
import com.delivery.service.DeliveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<?> createDelivery(@RequestBody Delivery delivery) {
        try {
            Delivery created = deliveryService.createDelivery(delivery);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Delivery>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<?> getDelivery(@PathVariable String trackingNumber) {
        try {
            return ResponseEntity.ok(deliveryService.getByTrackingNumber(trackingNumber));
        } catch (DeliveryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Delivery>> getByStatus(@PathVariable DeliveryStatus status) {
        return ResponseEntity.ok(deliveryService.getByStatus(status));
    }

    // ─── UPDATE STATUS ────────────────────────────────────────────────────────

    @PutMapping("/{trackingNumber}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String trackingNumber,
            @RequestBody StatusUpdateRequest request) {
        try {
            Delivery updated = deliveryService.updateStatus(
                trackingNumber, request.getStatus(), request.getRemarks());
            return ResponseEntity.ok(updated);
        } catch (DeliveryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (InvalidStatusTransitionException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @DeleteMapping("/{trackingNumber}")
    public ResponseEntity<?> deleteDelivery(@PathVariable String trackingNumber) {
        try {
            deliveryService.deleteDelivery(trackingNumber);
            return ResponseEntity.ok(Map.of("message", "Delivery deleted successfully"));
        } catch (DeliveryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // ─── HEALTH CHECK ─────────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "Delivery Tracking System"));
    }
}
