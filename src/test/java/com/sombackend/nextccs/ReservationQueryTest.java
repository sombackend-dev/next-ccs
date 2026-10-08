package com.sombackend.nextccs;

import com.sombackend.nextccs.entity.Reservation;
import com.sombackend.nextccs.entity.Vehicle;
import com.sombackend.nextccs.exception.InvalidReservationTimeException;
import com.sombackend.nextccs.repository.VehicleRepository;
import com.sombackend.nextccs.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReservationQueryTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    void 차량과_기간으로_예약을_조회한다() {
        Vehicle vehicle = vehicleRepository.save(
                new Vehicle(
                        "QUERY-" + UUID.randomUUID(),
                        "TEST-MODEL"
                )
        );

        LocalDateTime reservationStart =
                LocalDateTime.of(2031, 1, 10, 10, 0);

        LocalDateTime reservationEnd =
                LocalDateTime.of(2031, 1, 10, 12, 0);

        Reservation created = reservationService.create(
                vehicle.getId(),
                reservationStart,
                reservationEnd
        );

        List<Reservation> result =
                reservationService.findByVehicleAndPeriod(
                        vehicle.getId(),
                        LocalDateTime.of(2031, 1, 1, 0, 0),
                        LocalDateTime.of(2031, 2, 1, 0, 0)
                );

        assertTrue(
                result.stream()
                        .anyMatch(reservation ->
                                reservation.getId().equals(created.getId()))
        );
    }

    @Test
    void 조회_시작시간이_종료시간보다_늦으면_예외가_발생한다() {
        LocalDateTime startTime =
                LocalDateTime.of(2031, 2, 1, 0, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2031, 1, 1, 0, 0);

        assertThrows(
                InvalidReservationTimeException.class,
                () -> reservationService.findByVehicleAndPeriod(
                        1L,
                        startTime,
                        endTime
                )
        );
    }
}