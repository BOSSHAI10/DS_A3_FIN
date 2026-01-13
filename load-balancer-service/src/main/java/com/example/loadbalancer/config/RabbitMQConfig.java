package com.example.loadbalancer.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // AICI ESTE CHEIA: Numele cozii trebuie să fie EXACT "device_data_queue"
    // pentru că asta caută listener-ul tău.
    @Bean
    public Queue deviceDataQueue() {
        return new Queue("device_data_queue", true);
    }

    // Cozile pentru monitoring (destinație)
    @Bean
    public Queue monitoringQueue0() {
        return new Queue("monitoring_queue_0", true);
    }

    @Bean
    public Queue monitoringQueue1() {
        return new Queue("monitoring_queue_1", true);
    }

    @Bean
    public Queue monitoringQueue2() {
        return new Queue("monitoring_queue_2", true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}