package com.sombackend.nextccs.repository;

import com.sombackend.nextccs.entity.Reservation;
import com.sombackend.nextccs.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByVehicleAndStartTimeLessThanAndEndTimeGreaterThan(
            Vehicle vehicle,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    List<Reservation> findByVehicleIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(
            Long vehicleId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}