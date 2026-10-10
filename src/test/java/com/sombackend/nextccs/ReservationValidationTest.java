package com.sombackend.nextccs;

import com.sombackend.nextccs.entity.Reservation;
import com.sombackend.nextccs.entity.Vehicle;
import com.sombackend.nextccs.exception.ReservationConflictException;
import com.sombackend.nextccs.exception.VehicleNotFoundException;
import com.sombackend.nextccs.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.sombackend.nextccs.exception.InvalidReservationTimeException;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void missingVehicleIdReturns400() throws Exception {
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startTime": "2031-01-10T10:00:00",
                                  "endTime": "2031-01-10T12:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("차량 ID는 필수입니다."));

        verifyNoInteractions(reservationService);
    }

    @Test
    void missingStartTimeReturns400() throws Exception {
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": 1,
                                  "endTime": "2031-01-10T12:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("예약 시작 시간은 필수입니다."));

        verifyNoInteractions(reservationService);
    }

    @Test
    void missingEndTimeReturns400() throws Exception {
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": 1,
                                  "startTime": "2031-01-10T10:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("예약 종료 시간은 필수입니다."));

        verifyNoInteractions(reservationService);
    }

    @Test
    void successfulReservationReturns201() throws Exception {
        LocalDateTime start = LocalDateTime.of(2031, 1, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2031, 1, 10, 12, 0);

        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getId()).thenReturn(1L);

        Reservation reservation = mock(Reservation.class);
        when(reservation.getId()).thenReturn(100L);
        when(reservation.getVehicle()).thenReturn(vehicle);
        when(reservation.getStartTime()).thenReturn(start);
        when(reservation.getEndTime()).thenReturn(end);

        when(reservationService.create(eq(1L), eq(start), eq(end)))
                .thenReturn(reservation);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": 1,
                                  "startTime": "2031-01-10T10:00:00",
                                  "endTime": "2031-01-10T12:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.vehicleId").value(1));

        verify(reservationService).create(1L, start, end);
    }

    @Test
    void nonexistentVehicleReturns404() throws Exception {
        when(reservationService.create(anyLong(), any(), any()))
                .thenThrow(new VehicleNotFoundException("차량을 찾을 수 없습니다."));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": 999999,
                                  "startTime": "2031-01-10T10:00:00",
                                  "endTime": "2031-01-10T12:00:00"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void overlappingReservationReturns409() throws Exception {
        when(reservationService.create(anyLong(), any(), any()))
                .thenThrow(new ReservationConflictException(
                        "해당 시간에 이미 예약된 차량입니다."
                ));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": 1,
                                  "startTime": "2031-01-10T10:00:00",
                                  "endTime": "2031-01-10T12:00:00"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidReservationTimeReturns400() throws Exception {
        LocalDateTime start = LocalDateTime.of(2031, 1, 10, 12, 0);
        LocalDateTime end = LocalDateTime.of(2031, 1, 10, 10, 0);

        when(reservationService.create(eq(1L), eq(start), eq(end)))
                .thenThrow(new InvalidReservationTimeException(
                        "예약 시작 시간은 종료 시간보다 빨라야 합니다."
                ));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "vehicleId": 1,
                              "startTime": "2031-01-10T12:00:00",
                              "endTime": "2031-01-10T10:00:00"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(reservationService).create(1L, start, end);
    }
}