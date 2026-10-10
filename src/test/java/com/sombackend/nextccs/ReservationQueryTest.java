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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
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

    @Test
    void 예약_조회_시작시간은_포함하고_종료시간은_제외한다() {

        // given
        Vehicle vehicle = vehicleRepository.save(
                new Vehicle(
                        "BOUNDARY-" + UUID.randomUUID(),
                        "Boundary Test Vehicle"
                )
        );

        Long vehicleId = vehicle.getId();

        LocalDateTime queryStart =
                LocalDateTime.of(2031, 1, 10, 10, 0);

        LocalDateTime queryEnd =
                LocalDateTime.of(2031, 1, 10, 12, 0);

        // 조회 시작 이전: 제외
        reservationService.create(
                vehicleId,
                LocalDateTime.of(2031, 1, 10, 9, 59),
                LocalDateTime.of(2031, 1, 10, 10, 0)
        );

        // 조회 시작과 동일: 포함
        Reservation atStart = reservationService.create(
                vehicleId,
                LocalDateTime.of(2031, 1, 10, 10, 0),
                LocalDateTime.of(2031, 1, 10, 10, 30)
        );

        // 조회 종료 이전: 포함
        Reservation beforeEnd = reservationService.create(
                vehicleId,
                LocalDateTime.of(2031, 1, 10, 11, 59),
                LocalDateTime.of(2031, 1, 10, 12, 0)
        );

        // 조회 종료와 동일: 제외
        reservationService.create(
                vehicleId,
                LocalDateTime.of(2031, 1, 10, 12, 0),
                LocalDateTime.of(2031, 1, 10, 13, 0)
        );

        // when
        List<Reservation> result =
                reservationService.findByVehicleAndPeriod(
                        vehicleId,
                        queryStart,
                        queryEnd
                );

        // then
        assertEquals(2, result.size());

        assertTrue(
                result.stream().anyMatch(reservation ->
                        reservation.getId().equals(atStart.getId()))
        );

        assertTrue(
                result.stream().anyMatch(reservation ->
                        reservation.getId().equals(beforeEnd.getId()))
        );
    }
}