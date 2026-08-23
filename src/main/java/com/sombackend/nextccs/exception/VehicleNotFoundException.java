package com.sombackend.nextccs.exception;

public class VehicleNotFoundException extends RuntimeException{

    public VehicleNotFoundException(String message){
        super(message);
    }
}
