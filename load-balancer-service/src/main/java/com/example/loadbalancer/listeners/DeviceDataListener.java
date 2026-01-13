package com.example.loadbalancer.listeners;

import com.example.loadbalancer.dtos.DeviceDataDTO;
import com.example.loadbalancer.services.LoadBalancerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeviceDataListener {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceDataListener.class);
    
    @Autowired
    private LoadBalancerService loadBalancerService;
    
    @RabbitListener(queues = "${device.data.queue}")
    public void handleDeviceData(DeviceDataDTO deviceData) {
        try {
            logger.info("Received device data: {}", deviceData);
            loadBalancerService.routeDeviceData(deviceData);
        } catch (Exception e) {
            logger.error("Error processing device data: {}", e.getMessage(), e);
        }
    }
}
