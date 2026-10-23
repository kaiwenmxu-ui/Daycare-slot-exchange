package com.pilot.daycare.api;

import com.pilot.daycare.model.*;
import com.pilot.daycare.service.AlertService;
import com.pilot.daycare.service.TierService;
import com.pilot.daycare.store.InMemoryStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * GET /api/feed returns slots matching the caller's alert.
 * For a slot whose unlockTime > now, the server returns ONLY a stub.
 * Provider name, address, age, times — everything — is withheld.
 */
@RestController
@RequestMapping("/api")
public class FeedController {
    private final InMemoryStore store;
    private final TierService tiers;
    private final AlertService alerts;

    public FeedController(InMemoryStore store, TierService tiers, AlertService alerts) {
        this.store = store; this.tiers = tiers; this.alerts = alerts;
    }

    @GetMapping("/feed")
    public Map<String, Object> feed(HttpSession s) {
        String id = require(s);
        Alert a = store.alerts.get(id);
        Tier t = tiers.tierOf(id);
        Instant now = Instant.now();
        List<Map<String, Object>> items = new ArrayList<>();
        if (a != null) {
            for (SlotReport r : store.reports.values()) {
                if (!AlertService.matches(a, r)) continue;
                Instant unlock = alerts.unlockTime(id, r.reportedAt);
                if (now.isBefore(unlock)) {
                    long secs = unlock.getEpochSecond() - now.getEpochSecond();
                    items.add(Map.of(
                            "id", r.id,
                            "locked", true,
                            "unlocksAt", unlock.toString(),
                            "unlocksInSeconds", secs,
                            "minutesLeft", secs / 60,
                            "hoursLeft", secs / 3600,
                            "reportedAt", r.reportedAt.toString()
                    ));
                } else {
                    items.add(unlockedView(r));
                }
            }
        }
        return Map.of(
                "viewerTier", t.name(),
                "viewerLagHours", t.lagHours,
                "alertConfigured", a != null,
                "items", items
        );
    }

    private Map<String, Object> unlockedView(SlotReport r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.id);
        m.put("locked", false);
        m.put("providerName", r.providerName);
        m.put("ageGroup", r.ageGroup);
        m.put("addressHint", r.addressHint);
        m.put("startTime", r.startTime == null ? null : r.startTime.toString());
        m.put("endTime", r.endTime == null ? null : r.endTime.toString());
        m.put("notes", r.notes);
        m.put("status", r.status.name());
        m.put("confirmations", r.confirmations);
        m.put("verifications", r.verifications);
        m.put("reportedAt", r.reportedAt.toString());
        return m;
    }

    private String require(HttpSession s) {
        Object id = s.getAttribute("parentId");
        if (id == null) throw new IllegalStateException("unauthenticated");
        return (String) id;
    }
}