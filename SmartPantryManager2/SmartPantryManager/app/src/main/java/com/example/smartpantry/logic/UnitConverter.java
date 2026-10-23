package com.example.smartpantry.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts quantities to a base unit so "1 kg" in the pantry satisfies "500 g" in a recipe.
 *
 * Base units: grams (MASS), millilitres (VOLUME), and "items" (COUNT).
 * Different dimensions (e.g. grams vs pieces) can NOT be compared, so they never satisfy each other.
 */
public final class UnitConverter {

    public enum Dimension { MASS, VOLUME, COUNT }

    private static final Map<String, Dimension> DIMENSION = new HashMap<>();
    private static final Map<String, Double> FACTOR = new HashMap<>();

    private static void register(Dimension d, double factor, String... names) {
        for (String n : names) {
            DIMENSION.put(n, d);
            FACTOR.put(n, factor);
        }
    }

    static {
        // mass -> grams
        register(Dimension.MASS, 1, "g", "gram", "grams", "gm");
        register(Dimension.MASS, 1000, "kg", "kilogram", "kilograms", "kilo", "kilos");
        register(Dimension.MASS, 0.001, "mg", "milligram", "milligrams");
        register(Dimension.MASS, 28.3495, "oz", "ounce", "ounces");
        register(Dimension.MASS, 453.592, "lb", "lbs", "pound", "pounds");
        // volume -> millilitres
        register(Dimension.VOLUME, 1, "ml", "milliliter", "milliliters", "millilitre", "millilitres");
        register(Dimension.VOLUME, 1000, "l", "liter", "liters", "litre", "litres", "ltr");
        register(Dimension.VOLUME, 5, "tsp", "teaspoon", "teaspoons");
        register(Dimension.VOLUME, 15, "tbsp", "tbs", "tablespoon", "tablespoons");
        register(Dimension.VOLUME, 250, "cup", "cups");
        // countable things
        register(Dimension.COUNT, 1, "piece", "pieces", "pc", "pcs", "whole", "unit", "units", "each",
                "slice", "slices", "clove", "cloves", "can", "cans", "tin", "tins", "pack", "packs", "bunch", "");
    }

    private UnitConverter() { }

    /** Lower-cases and strips dots/spaces: " Tbsp. " -> "tbsp". */
    public static String clean(String unit) {
        return unit == null ? "" : unit.toLowerCase(Locale.ROOT).trim().replace(".", "");
    }

    public static Dimension dimensionOf(String unit) {
        Dimension d = DIMENSION.get(clean(unit));
        return d == null ? Dimension.COUNT : d; // unknown units are treated as plain counts
    }

    private static double factorOf(String unit) {
        Double f = FACTOR.get(clean(unit));
        return f == null ? 1.0 : f;
    }

    public static double toBase(double quantity, String unit) {
        return quantity * factorOf(unit);
    }

    public static double fromBase(double base, String unit) {
        return base / factorOf(unit);
    }
}
