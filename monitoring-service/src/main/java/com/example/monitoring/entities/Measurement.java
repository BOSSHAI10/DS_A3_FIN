package com.example.monitoring.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID deviceId;

    // Folosim LocalDateTime pentru a stoca data lizibil în DB
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Double energyConsumption;

    public Measurement() {}

    public Measurement(UUID deviceId, LocalDateTime timestamp, Double energyConsumption) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.energyConsumption = energyConsumption;
    }

    // --- GETTERS ȘI SETTERS ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public LocalDateTime getTimestamp() { return timestamp; }

    // Aici era problema: Tipul parametrului trebuie să fie LocalDateTime
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Double getEnergyConsumption() { return energyConsumption; }

    // Aici era problema: Metoda lipsea sau avea alt nume
    public void setEnergyConsumption(Double energyConsumption) { this.energyConsumption = energyConsumption; }
}