package com.pilot.daycare.service;

import com.pilot.daycare.model.Tier;
import com.pilot.daycare.store.InMemoryStore;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TierService {
    private final InMemoryStore store;

    public TierService(InMemoryStore store) { this.store = store; }

    public int creditsLast90Days(String parentId) {
        Instant cutoff = Instant.now().minus(Duration.ofDays(90));
        return store.ledger.stream()
                .filter(e -> e.parentId.equals(parentId))
                .filter(e -> e.createdAt.isAfter(cutoff))
                .mapToInt(e -> e.delta)
                .sum();
    }

    public Tier tierOf(String parentId) {
        return Tier.fromCredits(creditsLast90Days(parentId));
    }
}