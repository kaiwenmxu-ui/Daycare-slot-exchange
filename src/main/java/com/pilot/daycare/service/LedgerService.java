package com.pilot.daycare.service;

import com.pilot.daycare.model.LedgerEntry;
import com.pilot.daycare.store.InMemoryStore;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class LedgerService {
    public static final int WEEKLY_CAP = 20;
    private final InMemoryStore store;

    public LedgerService(InMemoryStore store) { this.store = store; }

    public void award(String parentId, int delta, LedgerEntry.Reason reason, String reportId) {
        if (delta > 0) {
            int already = earnedThisWeek(parentId);
            int room = Math.max(0, WEEKLY_CAP - already);
            delta = Math.min(delta, room);
            if (delta == 0) return;
        }
        store.ledger.add(new LedgerEntry(UUID.randomUUID().toString(),
                parentId, delta, reason, reportId));
    }

    public int earnedThisWeek(String parentId) {
        Instant cutoff = Instant.now().minus(Duration.ofDays(7));
        return store.ledger.stream()
                .filter(e -> e.parentId.equals(parentId))
                .filter(e -> e.createdAt.isAfter(cutoff))
                .filter(e -> e.delta > 0)
                .mapToInt(e -> e.delta).sum();
    }
}