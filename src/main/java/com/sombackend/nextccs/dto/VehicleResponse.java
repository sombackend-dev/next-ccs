package com.sombackend.nextccs.dto;

import com.sombackend.nextccs.entity.VehicleStatus;

public class VehicleResponse {

    private Long id;
    private String vehicleNumber;
    private String modelName;
    private VehicleStatus status;

    public VehicleResponse(
            Long id,
            String vehicleNumber,
            String modelName,
            VehicleStatus status
    ) {
        this.id = id;
        this.vehicleNumber = vehicleNumber;
        this.modelName = modelName;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getModelName() {
        return modelName;
    }

    public VehicleStatus getStatus() {
        return status;
    }
}