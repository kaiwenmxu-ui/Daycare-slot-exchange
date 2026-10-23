package com.pilot.daycare.api;

import com.pilot.daycare.model.Alert;
import com.pilot.daycare.store.InMemoryStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertSetupController {
    private final InMemoryStore store;

    public AlertSetupController(InMemoryStore store) { this.store = store; }

    @GetMapping
    public Object get(HttpSession s) {
        String id = require(s);
        Alert a = store.alerts.get(id);
        if (a == null) {
            return Map.of("configured", false);
        }
        return a;
    }

    @PostMapping
    public Alert save(@RequestBody Map<String, Object> body, HttpSession s) {
        String id = require(s);
        Alert a = new Alert(id,
                ((Number) body.get("lat")).doubleValue(),
                ((Number) body.get("lng")).doubleValue(),
                ((Number) body.getOrDefault("radiusKm", 10)).doubleValue(),
                (String) body.getOrDefault("ageGroup", "INFANT"));
        store.alerts.put(id, a);
        return a;
    }

    private String require(HttpSession s) {
        Object id = s.getAttribute("parentId");
        if (id == null) throw new IllegalStateException("unauthenticated");
        return (String) id;
    }
}