package com.sombackend.nextccs.exception;

public class DuplicateVehicleNumberException extends RuntimeException {

    public DuplicateVehicleNumberException(String message){
        super(message);
    }
}
