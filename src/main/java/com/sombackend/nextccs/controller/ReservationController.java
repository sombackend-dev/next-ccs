package com.sombackend.nextccs.controller;

import com.sombackend.nextccs.dto.ReservationCreateRequest;
import com.sombackend.nextccs.dto.ReservationResponse;
import com.sombackend.nextccs.entity.Reservation;
import com.sombackend.nextccs.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@Tag(
        name = "Reservation",
        description = "차량 예약 생성 및 기간별 예약 조회 API"
)
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(
            summary = "차량 예약 생성",
            description = "예약 시간이 유효한지 검증하고, 차량에 비관적 락을 적용하여 동시 예약 충돌을 방지합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "예약 생성 성공"),
            @ApiResponse(responseCode = "400", description = "예약 시간 오류"),
            @ApiResponse(responseCode = "404", description = "차량을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "예약 시간 충돌")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        Reservation reservation = reservationService.create(
                request.getVehicleId(),
                request.getStartTime(),
                request.getEndTime()
        );

        return toResponse(reservation);
    }

    @Operation(
            summary = "차량별 기간 예약 조회",
            description = "차량 ID와 기간을 기준으로 예약 시작 시간이 조회 구간에 포함되는 예약 목록을 반환합니다. 조회 구간은 시작 시간 포함, 종료 시간 제외입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "조회 기간 오류")
    })
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