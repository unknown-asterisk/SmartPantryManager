package com.example.smartpantry.model;

import com.example.smartpantry.logic.QuantityFormat;

/** One required ingredient line of a recipe. */
public class RecipeIngredient {
    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }

    /** e.g. "200 g pasta" or "3 x egg". */
    public String toDisplayString() {
        String q = QuantityFormat.format(quantity);
        if (unit == null || unit.isEmpty() || unit.equalsIgnoreCase("piece")) {
            return q + " x " + name;
        }
        return q + " " + unit + " " + name;
    }
}
