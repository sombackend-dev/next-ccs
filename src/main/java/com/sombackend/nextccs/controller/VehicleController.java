package com.sombackend.nextccs.controller;

import com.sombackend.nextccs.dto.VehicleCreateRequest;
import com.sombackend.nextccs.dto.VehicleResponse;
import com.sombackend.nextccs.dto.VehicleStatusUpdateRequest;
import com.sombackend.nextccs.entity.Vehicle;
import com.sombackend.nextccs.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

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

        return toResponse(savedVehicle);
    }

    @Operation(summary = "차량 단건 조회")
    @GetMapping("/{id}")
    public VehicleResponse findById(@PathVariable Long id) {
        return toResponse(vehicleService.findById(id));
    }

    @Operation(summary = "차량 목록 페이지 조회")
    @GetMapping
    public Page<VehicleResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("page는 0 이상이어야 합니다.");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size는 1 이상 100 이하여야 합니다.");
        }

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "id")
        );

        return vehicleService.findAll(pageable)
                .map(this::toResponse);
    }

    @Operation(summary = "차량 상태 변경")
    @PatchMapping("/{id}/status")
    public VehicleResponse updateStatus(
            @PathVariable Long id,
            @RequestBody VehicleStatusUpdateRequest request
    ) {
        return toResponse(
                vehicleService.updateStatus(id, request.getStatus())
        );
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getModelName(),
                vehicle.getStatus()
        );
    }
}