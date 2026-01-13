package com.example.loadbalancer.controllers;

import com.example.loadbalancer.dtos.DeviceDataDTO;
import com.example.loadbalancer.services.LoadBalancerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/load-balancer")
public class LoadBalancerController {
    
    @Autowired
    private LoadBalancerService loadBalancerService;
    
    @PostMapping("/route")
    public ResponseEntity<String> routeDeviceData(@RequestBody DeviceDataDTO deviceData) {
        try {
            loadBalancerService.routeDeviceData(deviceData);
            return ResponseEntity.ok("Device data routed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error routing device data: " + e.getMessage());
        }
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(loadBalancerService.getStatistics());
    }
    
    @PostMapping("/statistics/reset")
    public ResponseEntity<String> resetStatistics() {
        loadBalancerService.resetStatistics();
        return ResponseEntity.ok("Statistics reset successfully");
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> getHealth() {
        return ResponseEntity.ok(Map.of(
            "status", loadBalancerService.getHealthStatus(),
            "service", "Load Balancer Service"
        ));
    }
}
