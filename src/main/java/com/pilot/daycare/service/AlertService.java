package com.pilot.daycare.service;

import com.pilot.daycare.model.Alert;
import com.pilot.daycare.model.SlotReport;
import com.pilot.daycare.model.Tier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class AlertService {
    private final TierService tiers;

    public AlertService(TierService tiers) { this.tiers = tiers; }

    public static boolean matches(Alert a, SlotReport r) {
        if (!a.ageGroup.equalsIgnoreCase(r.ageGroup)) return false;
        return haversineKm(a.lat, a.lng, r.lat, r.lng) <= a.radiusKm;
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371, dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                 + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon/2)*Math.sin(dLon/2);
        return 2 * R * Math.asin(Math.sqrt(a));
    }

    public Instant unlockTime(String viewerId, Instant reportedAt) {
        Tier t = tiers.tierOf(viewerId);
        return reportedAt.plus(Duration.ofHours(t.lagHours));
    }

    public boolean visibleNow(String viewerId, Instant reportedAt) {
        return !Instant.now().isBefore(unlockTime(viewerId, reportedAt));
    }
}