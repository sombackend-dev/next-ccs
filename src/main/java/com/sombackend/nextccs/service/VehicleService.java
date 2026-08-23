package com.sombackend.nextccs.service;

import com.sombackend.nextccs.entity.Vehicle;
import com.sombackend.nextccs.entity.VehicleStatus;
import com.sombackend.nextccs.exception.DuplicateVehicleNumberException;
import com.sombackend.nextccs.exception.VehicleNotFoundException;
import com.sombackend.nextccs.repository.VehicleRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle save(Vehicle vehicle) {

        if(vehicleRepository.existsByVehicleNumber(vehicle.getVehicleNumber())){
            throw new DuplicateVehicleNumberException(("이미 등록된 차량번호입니다.."));
        }

        return vehicleRepository.save(vehicle);
    }

    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("존재하지 않는 차량입니다."));
    }

    public List<Vehicle> findAll(){
        return vehicleRepository.findAll();
    }

    @Transactional
    public Vehicle updateStatus(Long id, VehicleStatus status) {
        Vehicle vehicle = findById(id);
        vehicle.changeStatus(status);

        return vehicle;
    }




}