package com.demo;

public class App {
    
    public enum DeliveryStatus {
        ORDERED, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    }
    
    private String orderId;
    private DeliveryStatus currentStatus;
    
    public App(String orderId) {
        this.orderId = orderId;
        this.currentStatus = DeliveryStatus.ORDERED;
    }
    
    public DeliveryStatus getStatus() {
        return currentStatus;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public boolean updateStatus(DeliveryStatus newStatus) {
        // Define valid transitions
        switch (currentStatus) {
            case ORDERED:
                if (newStatus == DeliveryStatus.SHIPPED || 
                    newStatus == DeliveryStatus.CANCELLED) {
                    currentStatus = newStatus;
                    return true;
                }
                break;
            case SHIPPED:
                if (newStatus == DeliveryStatus.OUT_FOR_DELIVERY || 
                    newStatus == DeliveryStatus.CANCELLED) {
                    currentStatus = newStatus;
                    return true;
                }
                break;
            case OUT_FOR_DELIVERY:
                if (newStatus == DeliveryStatus.DELIVERED || 
                    newStatus == DeliveryStatus.CANCELLED) {
                    currentStatus = newStatus;
                    return true;
                }
                break;
            case DELIVERED:
            case CANCELLED:
                return false; // No transitions allowed from final states
        }
        return false; // Invalid transition
    }
    
    public static void main(String[] args) {
        App tracking = new App("ORD-001");
        System.out.println("Order: " + tracking.getOrderId());
        System.out.println("Initial Status: " + tracking.getStatus());
        
        tracking.updateStatus(DeliveryStatus.SHIPPED);
        System.out.println("After update: " + tracking.getStatus());
        
        tracking.updateStatus(DeliveryStatus.OUT_FOR_DELIVERY);
        System.out.println("After update: " + tracking.getStatus());
        
        tracking.updateStatus(DeliveryStatus.DELIVERED);
        System.out.println("Final Status: " + tracking.getStatus());
    }
}