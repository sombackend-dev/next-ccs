package com.sombackend.nextccs.exception;

public class InvalidReservationTimeException extends RuntimeException {

    public InvalidReservationTimeException(String message) {
        super(message);
    }
}