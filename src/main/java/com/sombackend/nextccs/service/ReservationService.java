package com.sombackend.nextccs.service;

import com.sombackend.nextccs.entity.Reservation;
import com.sombackend.nextccs.entity.Vehicle;
import com.sombackend.nextccs.exception.InvalidReservationTimeException;
import com.sombackend.nextccs.exception.ReservationConflictException;
import com.sombackend.nextccs.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final VehicleService vehicleService;

    public ReservationService(
            ReservationRepository reservationRepository,
            VehicleService vehicleService
    ) {
        this.reservationRepository = reservationRepository;
        this.vehicleService = vehicleService;
    }

    @Transactional
    public Reservation create(
            Long vehicleId,
            java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime
    ) {
        if(!startTime.isBefore(endTime)){
            throw new InvalidReservationTimeException(
                    "예약 시작 시간은 종료 시간보다 빨라야 합니다."
                    );
        }
        Vehicle vehicle = vehicleService.findByIdForUpdate(vehicleId);

        boolean exists = reservationRepository
                .existsByVehicleAndStartTimeLessThanAndEndTimeGreaterThan(
                        vehicle, endTime, startTime
                );

        if(exists){
            throw new ReservationConflictException(
                    "해당 시간에 이미 예약된 차량입니다."
            );
        }



        Reservation reservation = new Reservation(
                vehicle,
                startTime,
                endTime
        );

        return reservationRepository.save(reservation);
    }
}