package com.sombackend.nextccs.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
public class Reservation {

    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne@JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    protected Reservation(){

    }

    public Reservation(
            Vehicle vehicle,
            LocalDateTime startTime,
            LocalDateTime endTime
    ){
        this.vehicle = vehicle;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = ReservationStatus.RESERVED;
    }

    public Long getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
