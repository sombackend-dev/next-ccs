package com.sombackend.nextccs.service;

import com.sombackend.nextccs.entity.Vehicle;
import com.sombackend.nextccs.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle save(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 차량입니다."));
    }

    public List<Vehicle> findAll(){
        return vehicleRepository.findAll();
    }


}