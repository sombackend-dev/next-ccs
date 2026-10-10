package com.sombackend.nextccs;

import com.sombackend.nextccs.exception.InvalidReservationTimeException;
import com.sombackend.nextccs.repository.ReservationRepository;
import com.sombackend.nextccs.service.ReservationService;
import com.sombackend.nextccs.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private VehicleService vehicleService;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        vehicleService = mock(VehicleService.class);

        reservationService = new ReservationService(
                reservationRepository,
                vehicleService
        );
    }

    @Test
    void startTimeAfterEndTimeThrowsException() {
        LocalDateTime start = LocalDateTime.of(2031, 1, 10, 12, 0);
        LocalDateTime end = LocalDateTime.of(2031, 1, 10, 10, 0);

        assertThrows(
                InvalidReservationTimeException.class,
                () -> reservationService.create(1L, start, end)
        );

        verifyNoInteractions(reservationRepository, vehicleService);
    }

    @Test
    void sameStartAndEndTimeThrowsException() {
        LocalDateTime time = LocalDateTime.of(2031, 1, 10, 10, 0);

        assertThrows(
                InvalidReservationTimeException.class,
                () -> reservationService.create(1L, time, time)
        );

        verifyNoInteractions(reservationRepository, vehicleService);
    }

    @Test
    void invalidSearchPeriodThrowsException() {
        LocalDateTime start = LocalDateTime.of(2031, 1, 10, 12, 0);
        LocalDateTime end = LocalDateTime.of(2031, 1, 10, 10, 0);

        assertThrows(
                InvalidReservationTimeException.class,
                () -> reservationService.findByVehicleAndPeriod(
                        1L, start, end
                )
        );

        verifyNoInteractions(reservationRepository, vehicleService);
    }

    @Test
    void nullStartTimeThrowsException() {
        LocalDateTime end = LocalDateTime.of(2031, 1, 10, 12, 0);

        assertThrows(
                InvalidReservationTimeException.class,
                () -> reservationService.create(1L, null, end)
        );

        verifyNoInteractions(reservationRepository, vehicleService);
    }

    @Test
    void nullEndTimeThrowsException() {
        LocalDateTime start = LocalDateTime.of(2031, 1, 10, 10, 0);

        assertThrows(
                InvalidReservationTimeException.class,
                () -> reservationService.create(1L, start, null)
        );

        verifyNoInteractions(reservationRepository, vehicleService);
    }
}