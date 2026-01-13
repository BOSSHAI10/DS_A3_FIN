package com.example.monitoring.configs;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {

    // 1. EXISTENTE
    public static final String USER_SYNC_QUEUE = "user_queue";
    public static final String DEVICE_SYNC_QUEUE = "device_queue";

    // 2. NOI (Adăugate pentru Consumerul de măsurători)
    // Asigură-te că numele 'monitoring_queue' este același cu cel din application.properties -> monitoring.queue.name
    public static final String MONITORING_QUEUE = "monitoring_queue";
    public static final String ALERT_QUEUE = "alert.queue";

    @Bean
    public Queue userQueue() {
        return new Queue(USER_SYNC_QUEUE, true);
    }

    @Bean
    public Queue deviceQueue() {
        return new Queue(DEVICE_SYNC_QUEUE, true);
    }

    // --- BEAN-URI NOI ---
    @Bean
    public Queue monitoringQueue() {
        return new Queue(MONITORING_QUEUE, true);
    }

    @Bean
    public Queue alertQueue() {
        return new Queue(ALERT_QUEUE, true);
    }
    // --------------------

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper());
        return converter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();

        // Mapări existente
        idClassMapping.put("com.example.devices.dtos.DeviceSyncDTO", com.example.monitoring.dtos.DeviceSyncDTO.class);

        // --- MAPARE NOUĂ (CRITIC) ---
        // Aici trebuie să pui pachetul EXACT al DTO-ului din aplicația care TRIMITE datele (Simulatorul)
        // Exemplu: "com.example.sensor.dtos.MeasurementDTO" -> com.example.monitoring.dtos.MeasurementDTO.class
        // Dacă primești eroare "Could not resolve type id...", aici trebuie să completezi:

        // idClassMapping.put("com.producer.pachet.MeasurementDTO", com.example.monitoring.dtos.MeasurementDTO.class);

        classMapper.setIdClassMapping(idClassMapping);
        return classMapper;
    }
}