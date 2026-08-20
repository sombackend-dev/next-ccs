package com.sombackend.nextccs.controller;

import com.sombackend.nextccs.dto.VehicleCreateRequest;
import com.sombackend.nextccs.dto.VehicleResponse;
import com.sombackend.nextccs.entity.Vehicle;
import com.sombackend.nextccs.service.VehicleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public VehicleResponse create(@RequestBody VehicleCreateRequest request) {

        Vehicle vehicle = new Vehicle(
                request.getVehicleNumber(),
                request.getModelName()
        );

        Vehicle savedVehicle = vehicleService.save(vehicle);

        return new VehicleResponse(
                savedVehicle.getId(),
                savedVehicle.getVehicleNumber(),
                savedVehicle.getModelName(),
                savedVehicle.getStatus());
    }

    @GetMapping("/{id}")
    public VehicleResponse findById(@PathVariable Long id){
        Vehicle vehicle = vehicleService.findById(id);

        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getModelName(),
                vehicle.getStatus()
        );
    }

    @GetMapping
    public List<VehicleResponse> findAll(){
        return vehicleService.findAll().stream()
                .map(vehicle -> new VehicleResponse(
                        vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getModelName(),
                vehicle.getStatus()
        )).toList();

    }
}