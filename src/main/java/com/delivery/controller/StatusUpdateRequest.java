package com.delivery.controller;

import com.delivery.model.DeliveryStatus;

public class StatusUpdateRequest {
    private DeliveryStatus status;
    private String remarks;

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
