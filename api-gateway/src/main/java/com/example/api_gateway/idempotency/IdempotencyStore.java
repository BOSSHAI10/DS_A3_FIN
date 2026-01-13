package com.example.api_gateway.idempotency;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyStore {

    private final Map<String, Integer> store = new ConcurrentHashMap<>();

    // Metoda care lipsea sau avea alt nume
    public boolean contains(String key) {
        return store.containsKey(key);
    }

    public Integer get(String key) {
        return store.get(key);
    }

    public void set(String key, Integer status) {
        store.put(key, status);
    }
}