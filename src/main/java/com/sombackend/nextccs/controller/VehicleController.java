package com.sombackend.nextccs.controller;

import com.sombackend.nextccs.dto.VehicleCreateRequest;
import com.sombackend.nextccs.dto.VehicleResponse;
import com.sombackend.nextccs.dto.VehicleStatusUpdateRequest;
import com.sombackend.nextccs.entity.Vehicle;
import com.sombackend.nextccs.service.VehicleService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;


@Tag(
        name = "Vehicle",
        description = "차량 등록 및 조회 API"
)

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @Operation(summary = "차량 등록")
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

    @Operation(summary = "차량 단건 조회")
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

    @Operation(summary = "전체 차량 조회")
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


    @PatchMapping("/{id}/status")
    public VehicleResponse updateStatus(
            @PathVariable Long id,
            @RequestBody VehicleStatusUpdateRequest request
    ) {
        Vehicle vehicle = vehicleService.updateStatus(id, request.getStatus());

        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getModelName(),
                vehicle.getStatus()
        );
    }

}