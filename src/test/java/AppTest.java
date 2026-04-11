package com.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import com.demo.App.DeliveryStatus;

public class AppTest {
    
    private App delivery;
    
    @BeforeEach
    public void setUp() {
        delivery = new App("ORD-001");
    }
    
    // --- Status Update Validation Tests ---
    
    @Test
    public void testInitialStatusIsOrdered() {
        assertEquals(DeliveryStatus.ORDERED, delivery.getStatus());
    }
    
    @Test
    public void testOrderedToShipped() {
        assertTrue(delivery.updateStatus(DeliveryStatus.SHIPPED));
        assertEquals(DeliveryStatus.SHIPPED, delivery.getStatus());
    }
    
    @Test
    public void testShippedToOutForDelivery() {
        delivery.updateStatus(DeliveryStatus.SHIPPED);
        assertTrue(delivery.updateStatus(DeliveryStatus.OUT_FOR_DELIVERY));
        assertEquals(DeliveryStatus.OUT_FOR_DELIVERY, delivery.getStatus());
    }
    
    @Test
    public void testOutForDeliveryToDelivered() {
        delivery.updateStatus(DeliveryStatus.SHIPPED);
        delivery.updateStatus(DeliveryStatus.OUT_FOR_DELIVERY);
        assertTrue(delivery.updateStatus(DeliveryStatus.DELIVERED));
        assertEquals(DeliveryStatus.DELIVERED, delivery.getStatus());
    }
    
    @Test
    public void testFullDeliveryFlow() {
        assertEquals(DeliveryStatus.ORDERED, delivery.getStatus());
        delivery.updateStatus(DeliveryStatus.SHIPPED);
        delivery.updateStatus(DeliveryStatus.OUT_FOR_DELIVERY);
        delivery.updateStatus(DeliveryStatus.DELIVERED);
        assertEquals(DeliveryStatus.DELIVERED, delivery.getStatus());
    }
    
    // --- Invalid Status Transition Tests ---
    
    @Test
    public void testInvalidOrderedToDelivered() {
        assertFalse(delivery.updateStatus(DeliveryStatus.DELIVERED));
        assertEquals(DeliveryStatus.ORDERED, delivery.getStatus());
    }
    
    @Test
    public void testInvalidOrderedToOutForDelivery() {
        assertFalse(delivery.updateStatus(DeliveryStatus.OUT_FOR_DELIVERY));
        assertEquals(DeliveryStatus.ORDERED, delivery.getStatus());
    }
    
    @Test
    public void testInvalidShippedToDelivered() {
        delivery.updateStatus(DeliveryStatus.SHIPPED);
        assertFalse(delivery.updateStatus(DeliveryStatus.DELIVERED));
        assertEquals(DeliveryStatus.SHIPPED, delivery.getStatus());
    }
    
    @Test
    public void testNoTransitionFromDelivered() {
        delivery.updateStatus(DeliveryStatus.SHIPPED);
        delivery.updateStatus(DeliveryStatus.OUT_FOR_DELIVERY);
        delivery.updateStatus(DeliveryStatus.DELIVERED);
        assertFalse(delivery.updateStatus(DeliveryStatus.CANCELLED));
        assertEquals(DeliveryStatus.DELIVERED, delivery.getStatus());
    }
    
    @Test
    public void testCancelFromOrdered() {
        assertTrue(delivery.updateStatus(DeliveryStatus.CANCELLED));
        assertEquals(DeliveryStatus.CANCELLED, delivery.getStatus());
    }
    
    @Test
    public void testNoTransitionFromCancelled() {
        delivery.updateStatus(DeliveryStatus.CANCELLED);
        assertFalse(delivery.updateStatus(DeliveryStatus.SHIPPED));
        assertEquals(DeliveryStatus.CANCELLED, delivery.getStatus());
    }
}