package com.delivery.model;

/**
 * Enum representing all valid delivery statuses.
 * Defines valid transitions between statuses.
 */
public enum DeliveryStatus {
    PENDING,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED,
    CANCELLED;

    /**
     * Checks if transitioning from this status to the given status is valid.
     */
    public boolean canTransitionTo(DeliveryStatus newStatus) {
        switch (this) {
            case PENDING:
                return newStatus == PICKED_UP || newStatus == CANCELLED;
            case PICKED_UP:
                return newStatus == IN_TRANSIT || newStatus == FAILED || newStatus == CANCELLED;
            case IN_TRANSIT:
                return newStatus == OUT_FOR_DELIVERY || newStatus == FAILED;
            case OUT_FOR_DELIVERY:
                return newStatus == DELIVERED || newStatus == FAILED;
            case DELIVERED:
            case FAILED:
            case CANCELLED:
                return false; // terminal states
            default:
                return false;
        }
    }
}
