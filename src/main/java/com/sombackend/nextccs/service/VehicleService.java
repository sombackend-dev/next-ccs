package com.sombackend.nextccs.service;

import com.sombackend.nextccs.entity.Vehicle;
import com.sombackend.nextccs.repository.VehicleRepository;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle save(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }
}