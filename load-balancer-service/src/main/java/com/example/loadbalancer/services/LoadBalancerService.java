package com.example.loadbalancer.services;

import com.example.loadbalancer.dtos.DeviceDataDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LoadBalancerService {

    private final RabbitTemplate rabbitTemplate;

    // Statistici simple
    private final AtomicLong totalMessages = new AtomicLong(0);
    private final AtomicLong lastReset = new AtomicLong(System.currentTimeMillis());

    public LoadBalancerService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Logica de rutare a datelor.
     * Exemplu simplu: Trimite tot în coada de monitoring.
     * Aici poți adăuga logică de Sharding (ex: coada 1 pt ID par, coada 2 pt ID impar).
     */
    public void routeDeviceData(DeviceDataDTO data) {
        totalMessages.incrementAndGet();

        // Trimitem datele mai departe către Monitoring Service
        // Asigură-te că numele cozii (routing key) este corect configurat în RabbitMQConfig
        String routingKey = "monitoring_queue";

        rabbitTemplate.convertAndSend(routingKey, data);

        System.out.println("LoadBalancer: Routed data for device " + data.getDeviceId() + " to " + routingKey);
    }

    /**
     * Returnează statisticile curente.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessagesProcessed", totalMessages.get());
        stats.put("lastResetTimestamp", lastReset.get());
        stats.put("uptimeSeconds", (System.currentTimeMillis() - lastReset.get()) / 1000);
        return stats;
    }

    /**
     * Resetează contoarele.
     */
    public void resetStatistics() {
        totalMessages.set(0);
        lastReset.set(System.currentTimeMillis());
    }

    /**
     * Returnează starea serviciului.
     */
    public String getHealthStatus() {
        return "Load Balancer Service is RUNNING. Messages processed: " + totalMessages.get();
    }
}