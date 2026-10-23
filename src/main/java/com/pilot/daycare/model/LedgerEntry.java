package com.pilot.daycare.model;

import java.time.Instant;

public class LedgerEntry {
    public enum Reason { REPORT, CONFIRM, VERIFY, FAKE_PENALTY }

    public String id;
    public String parentId;
    public int delta;
    public Reason reason;
    public String slotReportId;
    public Instant createdAt = Instant.now();

    public LedgerEntry(String id, String parentId, int delta, Reason reason, String slotReportId) {
        this.id = id;
        this.parentId = parentId;
        this.delta = delta;
        this.reason = reason;
        this.slotReportId = slotReportId;
    }
}