package com.sombackend.nextccs.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleNumber;

    private String modelName;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    protected Vehicle() {
    }


    public Vehicle(String vehicleNumber, String modelName) {
        this.vehicleNumber = vehicleNumber;
        this.modelName = modelName;
        this.status = VehicleStatus.AVAILABLE;
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