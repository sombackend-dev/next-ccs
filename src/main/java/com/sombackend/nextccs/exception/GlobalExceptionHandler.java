package com.sombackend.nextccs.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleVehicleNotFoundException(
            VehicleNotFoundException e) {

        Map<String, Object> body = Map.of(
                "status", HttpStatus.NOT_FOUND.value(),
                "message", e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    @ExceptionHandler(DuplicateVehicleNumberException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateVehicleNumberException(
            DuplicateVehicleNumberException e) {

        Map<String, Object> body = Map.of(
                "status", HttpStatus.CONFLICT.value(),
                "message", e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException e) {

        Map<String, Object> body = Map.of(
                "status", HttpStatus.CONFLICT.value(),
                "message", "이미 등록된 차량번호입니다."
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);


    }

}