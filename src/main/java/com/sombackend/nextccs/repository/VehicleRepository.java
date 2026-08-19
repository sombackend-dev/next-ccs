package com.sombackend.nextccs.repository;

import com.sombackend.nextccs.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}