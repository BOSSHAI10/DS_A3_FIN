package com.example.monitoring.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class MeasurementDTO implements Serializable {

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("measurementValue")
    private Double measurementValue;

    public MeasurementDTO() {}

    public MeasurementDTO(String deviceId, Long timestamp, Double measurementValue) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.measurementValue = measurementValue;
    }

    // --- GETTERS ȘI SETTERS (Esențiali pentru a scăpa de erorile "Cannot resolve method") ---

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getMeasurementValue() {
        return measurementValue;
    }

    public void setMeasurementValue(Double measurementValue) {
        this.measurementValue = measurementValue;
    }

    @Override
    public String toString() {
        return "MeasurementDTO{" +
                "deviceId='" + deviceId + '\'' +
                ", timestamp=" + timestamp +
                ", measurementValue=" + measurementValue +
                '}';
    }
}