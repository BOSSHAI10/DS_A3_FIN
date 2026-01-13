package com.example.loadbalancer.strategies;

import com.example.loadbalancer.dtos.DeviceDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConsistentHashStrategy implements LoadBalancingStrategy {
    
    @Value("${monitoring.replicas.count}")
    private int replicaCount;
    
    private final List<String> replicaQueues = List.of(
        "monitoring-queue-1",
        "monitoring-queue-2", 
        "monitoring-queue-3"
    );
    
    @Override
    public String selectQueue(DeviceDataDTO deviceData) {
        if (deviceData == null || deviceData.getDeviceId() == null) {
            return replicaQueues.get(0); // Default to first replica
        }
        
        // Use consistent hashing based on device ID
        int hash = deviceData.getDeviceId().hashCode();
        int replicaIndex = Math.abs(hash) % replicaCount;
        
        return replicaQueues.get(replicaIndex);
    }
    
    @Override
    public String getStrategyName() {
        return "CONSISTENT_HASH";
    }
}
