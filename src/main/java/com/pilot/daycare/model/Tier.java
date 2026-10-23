package com.pilot.daycare.model;

public enum Tier {
    NEW(0, 4, 24),
    CONTRIBUTOR(5, 14, 12),
    TRUSTED(15, Integer.MAX_VALUE, 0);

    public final int minCredits;
    public final int maxCredits;
    public final int lagHours;

    Tier(int minCredits, int maxCredits, int lagHours) {
        this.minCredits = minCredits;
        this.maxCredits = maxCredits;
        this.lagHours = lagHours;
    }

    public static Tier fromCredits(int credits) {
        if (credits >= TRUSTED.minCredits) return TRUSTED;
        if (credits >= CONTRIBUTOR.minCredits) return CONTRIBUTOR;
        return NEW;
    }
}