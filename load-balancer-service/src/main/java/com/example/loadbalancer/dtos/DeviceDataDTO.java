package com.example.loadbalancer.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class DeviceDataDTO implements Serializable {

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("timestamp")
    private Long timestamp; // Folosim Long pentru compatibilitate maximă

    @JsonProperty("measurementValue")
    private Double measurementValue;

    // Constructors
    public DeviceDataDTO() {}

    public DeviceDataDTO(String deviceId, Long timestamp, Double measurementValue) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.measurementValue = measurementValue;
    }

    // Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public Double getMeasurementValue() { return measurementValue; }
    public void setMeasurementValue(Double measurementValue) { this.measurementValue = measurementValue; }

    @Override
    public String toString() {
        return "DeviceDataDTO{" +
                "deviceId='" + deviceId + '\'' +
                ", timestamp=" + timestamp +
                ", value=" + measurementValue +
                '}';
    }
}