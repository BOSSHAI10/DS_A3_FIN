package com.example.loadbalancer.strategies;

import com.example.loadbalancer.dtos.DeviceDataDTO;

public interface LoadBalancingStrategy {
    
    /**
     * Selects the target queue for a device data message
     * @param deviceData the device data to route
     * @return the queue name for the target monitoring replica
     */
    String selectQueue(DeviceDataDTO deviceData);
    
    /**
     * Gets the name of the strategy
     * @return strategy name
     */
    String getStrategyName();
}
