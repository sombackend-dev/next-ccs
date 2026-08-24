package com.sombackend.nextccs.repository;

import com.sombackend.nextccs.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sombackend.nextccs.entity.Vehicle;
import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByVehicleAndStartTimeLessThanAndEndTimeGreaterThan(
            Vehicle vehicle,
            LocalDateTime endTime,
            LocalDateTime startTime
    );
}