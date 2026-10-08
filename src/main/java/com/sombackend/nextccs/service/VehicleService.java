package com.sombackend.nextccs.service;

import com.sombackend.nextccs.entity.Vehicle;
import com.sombackend.nextccs.entity.VehicleStatus;
import com.sombackend.nextccs.exception.DuplicateVehicleNumberException;
import com.sombackend.nextccs.exception.VehicleNotFoundException;
import com.sombackend.nextccs.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle save(Vehicle vehicle) {
        if (vehicleRepository.existsByVehicleNumber(vehicle.getVehicleNumber())) {
            throw new DuplicateVehicleNumberException("이미 등록된 차량번호입니다.");
        }

        return vehicleRepository.save(vehicle);
    }

    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new VehicleNotFoundException("존재하지 않는 차량입니다.")
                );
    }

    @Transactional(readOnly = true)
    public Page<Vehicle> findAll(Pageable pageable) {
        return vehicleRepository.findAll(pageable);
    }

    @Transactional
    public Vehicle updateStatus(Long id, VehicleStatus status) {
        Vehicle vehicle = findById(id);
        vehicle.changeStatus(status);

        return vehicle;
    }

    public Vehicle findByIdForUpdate(Long id) {
        return vehicleRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new VehicleNotFoundException("차량을 찾을 수 없습니다.")
                );
    }
}