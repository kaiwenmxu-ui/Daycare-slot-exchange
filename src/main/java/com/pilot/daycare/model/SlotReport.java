package com.pilot.daycare.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class SlotReport {
    public enum Status { PENDING, VERIFIED, REJECTED }

    public String id;
    public String reporterId;
    public String providerName;
    public String ageGroup;   // INFANT | TODDLER | PRESCHOOL
    public double lat;
    public double lng;
    public String addressHint;
    public Instant startTime;
    public Instant endTime;
    public String notes;
    public String screenshotName;
    public Instant reportedAt = Instant.now();

    public Status status = Status.PENDING;
    public int confirmations = 0;
    public int verifications = 0;
    public int flags = 0;

    public Map<String, Boolean> screenshotFields = new HashMap<>();
}
