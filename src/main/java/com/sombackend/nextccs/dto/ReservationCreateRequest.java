package com.sombackend.nextccs.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ReservationCreateRequest {

    @NotNull(message = "차량 ID는 필수입니다.")
    private Long vehicleId;

    @NotNull(message = "예약 시작 시간은 필수입니다.")
    private LocalDateTime startTime;

    @NotNull(message = "예약 종료 시간은 필수입니다.")
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