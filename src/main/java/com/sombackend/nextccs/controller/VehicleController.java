package com.sombackend.nextccs.controller;

import com.sombackend.nextccs.dto.VehicleCreateRequest;
import com.sombackend.nextccs.entity.Vehicle;
import com.sombackend.nextccs.service.VehicleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public Vehicle create(@RequestBody VehicleCreateRequest request) {

        Vehicle vehicle = new Vehicle(
                request.getVehicleNumber(),
                request.getModelName()
        );

        return vehicleService.save(vehicle);
    }
}