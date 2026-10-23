package com.pilot.daycare.api;

import com.pilot.daycare.model.Alert;
import com.pilot.daycare.model.SlotReport;
import com.pilot.daycare.service.ReportService;
import com.pilot.daycare.store.InMemoryStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * Convenience endpoint for the two-iframe demo. Seeds both demo accounts'
 * alerts at the Phoenix center, then posts a slot inside that geofence.
 * Only used when you click "Post a demo slot now".
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {
    private final InMemoryStore store;
    private final ReportService reports;

    public DemoController(InMemoryStore store, ReportService reports) {
        this.store = store; this.reports = reports;
    }

    @PostMapping("/seed-slot")
    public Map<String, Object> seedSlot(HttpSession s) {
        require(s);

        // Both demo accounts get an identical alert at Phoenix center
        Alert a = new Alert("trusted", 33.4484, -112.0740, 10, "INFANT");
        store.alerts.put("trusted", a);
        store.alerts.put("newbie", new Alert("newbie", 33.4484, -112.0740, 10, "INFANT"));

        SlotReport r = new SlotReport();
        r.providerName = "Sunrise Infant Care";
        r.ageGroup = "INFANT";
        r.lat = 33.4490;
        r.lng = -112.0745;
        r.addressHint = "3rd St & Osborn";
        r.startTime = Instant.now().plusSeconds(3600 * 8);
        r.endTime = Instant.now().plusSeconds(3600 * 20);
        r.notes = "Demo slot — one infant opening starting this week.";
        reports.create(r);
        return Map.of("ok", true, "id", r.id, "provider", r.providerName);
    }

    private String require(HttpSession s) {
        Object id = s.getAttribute("parentId");
        if (id == null) throw new IllegalStateException("unauthenticated");
        return (String) id;
    }
}