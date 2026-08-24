package com.sombackend.nextccs.dto;

import com.sombackend.nextccs.entity.ReservationStatus;

import java.time.LocalDateTime;

public class ReservationResponse {

    private Long id;
    private Long vehicleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;

    public ReservationResponse(
            Long id,
            Long vehicleId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            ReservationStatus status
    ) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}