package com.sombackend.nextccs.dto;

import java.time.LocalDateTime;

public class ReservationCreateRequest {

    private Long vehicleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Long getVehicleId() {
        return vehicleId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}