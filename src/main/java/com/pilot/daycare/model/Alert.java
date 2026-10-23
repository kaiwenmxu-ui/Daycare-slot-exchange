package com.pilot.daycare.model;

public class Alert {
    public String parentId;
    public double lat;
    public double lng;
    public double radiusKm;
    public String ageGroup;

    public Alert(String parentId, double lat, double lng, double radiusKm, String ageGroup) {
        this.parentId = parentId;
        this.lat = lat;
        this.lng = lng;
        this.radiusKm = radiusKm;
        this.ageGroup = ageGroup;
    }
}