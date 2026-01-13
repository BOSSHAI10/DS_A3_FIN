package com.example.chat.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class AlertConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AlertConsumer.class);

    private final SimpMessagingTemplate template;

    public AlertConsumer(SimpMessagingTemplate template) {
        this.template = template;
    }

    // Ascultă coada "alert.queue" (definită și în Monitoring Service)
    @RabbitListener(queues = "alert.queue")
    public void receiveAlert(String message) {
        logger.info("Alert received from RabbitMQ: {}", message);

        // Trimitem mesajul la toți clienții conectați la topicul "/topic/alerts"
        this.template.convertAndSend("/topic/alerts", message);

        logger.info("Alert forwarded to WebSocket clients via /topic/alerts");
    }
}