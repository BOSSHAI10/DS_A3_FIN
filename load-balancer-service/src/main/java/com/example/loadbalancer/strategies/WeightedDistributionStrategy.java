package com.example.loadbalancer.strategies;

import com.example.loadbalancer.dtos.DeviceDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WeightedDistributionStrategy implements LoadBalancingStrategy {
    
    @Value("${monitoring.replicas.count}")
    private int replicaCount;
    
    private final List<String> replicaQueues = List.of(
        "monitoring-queue-1",
        "monitoring-queue-2",
        "monitoring-queue-3"
    );
    
    // Weights for each replica (can be adjusted based on capacity)
    private final List<Integer> weights = List.of(1, 1, 1); // Equal weights by default
    
    private final AtomicInteger currentCounter = new AtomicInteger(0);
    
    @Override
    public String selectQueue(DeviceDataDTO deviceData) {
        // Simple weighted round-robin implementation
        int totalWeight = weights.stream().mapToInt(Integer::intValue).sum();
        int counter = currentCounter.getAndIncrement() % totalWeight;
        
        int cumulativeWeight = 0;
        for (int i = 0; i < replicaCount; i++) {
            cumulativeWeight += weights.get(i);
            if (counter < cumulativeWeight) {
                return replicaQueues.get(i);
            }
        }
        
        return replicaQueues.get(0); // Fallback
    }
    
    @Override
    public String getStrategyName() {
        return "WEIGHTED_DISTRIBUTION";
    }
}
