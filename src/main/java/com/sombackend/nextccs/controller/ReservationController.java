package com.sombackend.nextccs.controller;

import com.sombackend.nextccs.dto.ReservationCreateRequest;
import com.sombackend.nextccs.dto.ReservationResponse;
import com.sombackend.nextccs.entity.Reservation;
import com.sombackend.nextccs.service.ReservationService;
import org.springframework.web.bind.annotation.*;

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

        return new ReservationResponse(
                reservation.getId(),
                reservation.getVehicle().getId(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus()
        );
    }
}