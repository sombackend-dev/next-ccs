package com.sombackend.nextccs.controller;

import com.sombackend.nextccs.dto.ReservationCreateRequest;
import com.sombackend.nextccs.dto.ReservationResponse;
import com.sombackend.nextccs.entity.Reservation;
import com.sombackend.nextccs.service.ReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ReservationResponse create(
            @RequestBody ReservationCreateRequest request
    ) {
        Reservation reservation = reservationService.create(
                request.getVehicleId(),
                request.getStartTime(),
                request.getEndTime()
        );

        return toResponse(reservation);
    }

    @GetMapping
    public List<ReservationResponse> findByVehicleAndPeriod(
            @RequestParam Long vehicleId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endTime
    ) {
        return reservationService
                .findByVehicleAndPeriod(vehicleId, startTime, endTime)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getVehicle().getId(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus()
        );
    }
}