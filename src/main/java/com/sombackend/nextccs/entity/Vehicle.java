package com.sombackend.nextccs.entity;

import jakarta.persistence.*;

@Entity
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String vehicleNumber;

    @Column(nullable = false)
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

    public void changeStatus(VehicleStatus status){
        this.status = status;
    }


}