package com.example.monitoring.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import com.example.monitoring.dtos.MeasurementDTO;
import com.example.monitoring.entities.Measurement;
import com.example.monitoring.repositories.MeasurementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // Sau Date, în funcție de ce folosești

@Service
public class MeasurementConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MeasurementConsumer.class);

    private final MeasurementRepository measurementRepository;
    private final RabbitTemplate rabbitTemplate;

    // Limita hardcodată pentru demo
    private static final double HOURLY_LIMIT = 20.0;

    public MeasurementConsumer(MeasurementRepository measurementRepository, RabbitTemplate rabbitTemplate) {
        this.measurementRepository = measurementRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${monitoring.queue.name}")
    public void consumeMessage(MeasurementDTO message) {
        try {
            logger.info("Received measurement for device: {}", message.getDeviceId());

            Measurement entity = new Measurement();

            // 1. FIX UUID: Convertim din String în UUID
            entity.setDeviceId(UUID.fromString(message.getDeviceId()));

            // 2. FIX Timestamp: Convertim din Long (epoch) în LocalDateTime
            // Presupunem că timestamp-ul e în milisecunde
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(message.getTimestamp()),
                    ZoneId.systemDefault()
            );
            entity.setTimestamp(dateTime);

            // 3. FIX Value: Verifică numele câmpului în clasa Measurement!
            // Dacă primești eroare aici, probabil câmpul în entitate se numește "value"
            // Încearcă: entity.setValue(message.getMeasurementValue());
            entity.setEnergyConsumption(message.getMeasurementValue());

            measurementRepository.save(entity);
            logger.info("Measurement saved successfully.");

            // LOGICA DE ALERTĂ
            if (message.getMeasurementValue() > HOURLY_LIMIT) {
                String alertMsg = "Alert: High consumption for device " + message.getDeviceId()
                        + " Value: " + message.getMeasurementValue();
                logger.warn(alertMsg);
                rabbitTemplate.convertAndSend("alert.queue", alertMsg);
            }

        } catch (Exception e) {
            logger.error("Error processing measurement: {}", e.getMessage(), e);
        }
    }
}