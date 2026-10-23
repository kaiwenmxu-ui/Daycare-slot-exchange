package com.pilot.daycare.model;

import java.time.Instant;

public class Parent {
    public String id;
    public String displayName;
    public Instant createdAt = Instant.now();

    public Parent(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }
}
