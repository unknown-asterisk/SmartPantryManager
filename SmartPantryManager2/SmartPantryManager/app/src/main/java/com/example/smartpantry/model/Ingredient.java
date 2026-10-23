package com.example.smartpantry.model;

import com.example.smartpantry.util.DateUtils;

/** One item in the user's pantry (a row in the "pantry" table). */
public class Ingredient {
    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiryDate; // ISO yyyy-MM-dd, or null when the user gave none

    public Ingredient() { }

    public Ingredient(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    /** Days until expiry (negative if expired); null when there is no expiry date. */
    public Long daysUntilExpiry() {
        return DateUtils.daysUntil(expiryDate);
    }

    public boolean isExpired() {
        Long d = daysUntilExpiry();
        return d != null && d < 0;
    }
}
