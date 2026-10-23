package com.pilot.daycare.store;

import com.pilot.daycare.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryStore {
    public final Map<String, Parent> parents = new ConcurrentHashMap<>();
    public final Map<String, SlotReport> reports = new ConcurrentHashMap<>();
    public final List<LedgerEntry> ledger = Collections.synchronizedList(new ArrayList<>());
    public final Map<String, Alert> alerts = new ConcurrentHashMap<>();

    public InMemoryStore() {
        parents.put("trusted", new Parent("trusted", "Ana (Trusted)"));
        parents.put("newbie",  new Parent("newbie",  "Ben (New)"));
        // Seed the trusted account to Trusted tier (15+ credits in last 90 days)
        for (int i = 0; i < 16; i++) {
            ledger.add(new LedgerEntry(UUID.randomUUID().toString(), "trusted", 1,
                    LedgerEntry.Reason.VERIFY, "seed"));
        }
    }
}