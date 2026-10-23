package com.pilot.daycare.api;

import com.pilot.daycare.model.LedgerEntry;
import com.pilot.daycare.model.Tier;
import com.pilot.daycare.service.LedgerService;
import com.pilot.daycare.service.TierService;
import com.pilot.daycare.store.InMemoryStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final InMemoryStore store;
    private final TierService tiers;
    private final LedgerService ledger;

    public ProfileController(InMemoryStore store, TierService tiers, LedgerService ledger) {
        this.store = store; this.tiers = tiers; this.ledger = ledger;
    }

    @GetMapping
    public Map<String, Object> me(HttpSession session) {
        String id = require(session);
        int credits = tiers.creditsLast90Days(id);
        Tier t = Tier.fromCredits(credits);
        List<Map<String, Object>> recent = store.ledger.stream()
                .filter(e -> e.parentId.equals(id))
                .sorted(Comparator.comparing((LedgerEntry e) -> e.createdAt).reversed())
                .limit(15)
                .map(e -> Map.<String,Object>of(
                        "delta", e.delta,
                        "reason", e.reason.name(),
                        "at", e.createdAt.toString(),
                        "reportId", e.slotReportId == null ? "" : e.slotReportId))
                .collect(Collectors.toList());

        return Map.of(
                "id", id,
                "credits90d", credits,
                "tier", t.name(),
                "lagHours", t.lagHours,
                "weeklyEarned", ledger.earnedThisWeek(id),
                "weeklyCap", LedgerService.WEEKLY_CAP,
                "nextTierAt", t == Tier.NEW ? 5 : t == Tier.CONTRIBUTOR ? 15 : -1,
                "ledger", recent);
    }

    private String require(HttpSession s) {
        Object id = s.getAttribute("parentId");
        if (id == null) throw new IllegalStateException("unauthenticated");
        return (String) id;
    }
}