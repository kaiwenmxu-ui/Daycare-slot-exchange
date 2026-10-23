package com.pilot.daycare.service;

import com.pilot.daycare.model.Parent;
import com.pilot.daycare.store.InMemoryStore;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/** New-account throttle: <1h old → 1 report/hour; <24h old → 3 reports/day. */
@Service
public class RateLimiter {
    private final InMemoryStore store;

    public RateLimiter(InMemoryStore store) { this.store = store; }

    public boolean canReport(String parentId) {
        Parent p = store.parents.get(parentId);
        if (p == null) return false;
        long ageMin = Duration.between(p.createdAt, Instant.now()).toMinutes();
        if (ageMin < 60)  return count(parentId, Duration.ofMinutes(60)) < 1;
        if (ageMin < 1440) return count(parentId, Duration.ofMinutes(1440)) < 3;
        return true;
    }

    private long count(String parentId, Duration window) {
        Instant cutoff = Instant.now().minus(window);
        return store.reports.values().stream()
                .filter(r -> r.reporterId.equals(parentId))
                .filter(r -> r.reportedAt.isAfter(cutoff))
                .count();
    }
}