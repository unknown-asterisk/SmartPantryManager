package com.example.smartpantry.logic;

import com.example.smartpantry.logic.UnitConverter.Dimension;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.model.MatchResult;
import com.example.smartpantry.model.MatchResult.IngredientCheck;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * THE core business logic of the app: the STRICT-MATCHING RULE (assignment section 2.3).
 *
 * A recipe is "suggested" only if EVERY ingredient it requires is in the pantry in at least
 * the required quantity. One missing / insufficient ingredient => the recipe is excluded.
 *
 * Steps:
 *  1. buildPantryTotals  - normalise names + convert units, then SUM duplicate pantry entries.
 *  2. evaluate           - check each recipe ingredient against those totals.
 *  3. strictMatches      - keep only recipes with ZERO missing ingredients.
 *  4. almostThere        - (bonus) recipes with EXACTLY ONE missing ingredient, kept separate.
 */
public final class RecipeMatcher {

    /** Tolerance so floating-point rounding (e.g. 0.1 + 0.2) never causes a false "missing". */
    private static final double EPSILON = 1e-6;

    private RecipeMatcher() { }

    private static String key(String normalizedName, Dimension dimension) {
        return normalizedName + "|" + dimension.name();
    }

    /**
     * Totals the pantry per (ingredient, dimension) in base units.
     * Two entries "eggs 2 piece" + "Egg 4 pieces" become 6 eggs.
     *
     * @param ignoreExpired when true, items past their expiry date are not usable for cooking
     */
    public static Map<String, Double> buildPantryTotals(List<Ingredient> pantry, boolean ignoreExpired) {
        Map<String, Double> totals = new HashMap<>();
        for (Ingredient item : pantry) {
            if (ignoreExpired && item.isExpired()) continue;
            if (item.getQuantity() <= 0) continue;
            String name = IngredientNormalizer.normalize(item.getName());
            if (name.isEmpty()) continue;

            String k = key(name, UnitConverter.dimensionOf(item.getUnit()));
            double base = UnitConverter.toBase(item.getQuantity(), item.getUnit());
            Double current = totals.get(k);
            totals.put(k, (current == null ? 0.0 : current) + base);
        }
        return totals;
    }

    /** Checks every ingredient of one recipe against the pantry totals. */
    public static MatchResult evaluate(Recipe recipe, Map<String, Double> pantryTotals) {
        List<IngredientCheck> checks = new ArrayList<>();

        for (RecipeIngredient need : recipe.getIngredients()) {
            String name = IngredientNormalizer.normalize(need.getName());
            Dimension dim = UnitConverter.dimensionOf(need.getUnit());
            double needBase = UnitConverter.toBase(need.getQuantity(), need.getUnit());
            Double haveBase = pantryTotals.get(key(name, dim));

            if (haveBase != null && haveBase + EPSILON >= needBase) {
                checks.add(new IngredientCheck(need, true, "in pantry"));
            } else if (haveBase != null) {
                String have = QuantityFormat.format(UnitConverter.fromBase(haveBase, need.getUnit()));
                String want = QuantityFormat.format(need.getQuantity());
                String unit = need.getUnit() == null ? "" : need.getUnit();
                checks.add(new IngredientCheck(need, false,
                        "not enough (have " + have + ", need " + want + " " + unit + ")"));
            } else if (existsInOtherDimension(pantryTotals, name, dim)) {
                checks.add(new IngredientCheck(need, false,
                        "in pantry, but in a unit that can't be compared (" + need.getUnit() + " needed)"));
            } else {
                checks.add(new IngredientCheck(need, false, "not in pantry"));
            }
        }
        return new MatchResult(recipe, checks);
    }

    private static boolean existsInOtherDimension(Map<String, Double> totals, String name, Dimension skip) {
        for (Dimension d : Dimension.values()) {
            if (d != skip && totals.containsKey(key(name, d))) return true;
        }
        return false;
    }

    /**
     * Evaluates every recipe and tags how many of its ingredients are about to expire
     * (so the UI can prioritise recipes that rescue food).
     */
    public static List<MatchResult> evaluateAll(List<Recipe> recipes, List<Ingredient> pantry,
                                                boolean ignoreExpired, int expiringWithinDays) {
        Map<String, Double> totals = buildPantryTotals(pantry, ignoreExpired);

        Set<String> expiringNames = new HashSet<>();
        for (Ingredient item : pantry) {
            Long days = item.daysUntilExpiry();
            if (days != null && days >= 0 && days <= expiringWithinDays) {
                expiringNames.add(IngredientNormalizer.normalize(item.getName()));
            }
        }

        List<MatchResult> results = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult r = evaluate(recipe, totals);
            int expiring = 0;
            for (RecipeIngredient ri : recipe.getIngredients()) {
                if (expiringNames.contains(IngredientNormalizer.normalize(ri.getName()))) expiring++;
            }
            r.setExpiringIngredientCount(expiring);
            results.add(r);
        }
        return results;
    }

    /** The main "Suggested Recipes" list: ONLY recipes with nothing missing. */
    public static List<MatchResult> strictMatches(List<MatchResult> all) {
        List<MatchResult> out = new ArrayList<>();
        for (MatchResult r : all) {
            if (r.isStrictMatch()) out.add(r);
        }
        Collections.sort(out, new Comparator<MatchResult>() {
            @Override
            public int compare(MatchResult a, MatchResult b) {
                // recipes that use soon-to-expire food come first, then A-Z
                int byExpiring = b.getExpiringIngredientCount() - a.getExpiringIngredientCount();
                if (byExpiring != 0) return byExpiring;
                return a.getRecipe().getName().toLowerCase(Locale.ROOT)
                        .compareTo(b.getRecipe().getName().toLowerCase(Locale.ROOT));
            }
        });
        return out;
    }

    /** Bonus list: recipes missing exactly ONE ingredient. Never mixed into strictMatches(). */
    public static List<MatchResult> almostThere(List<MatchResult> all) {
        List<MatchResult> out = new ArrayList<>();
        for (MatchResult r : all) {
            if (!r.getChecks().isEmpty() && r.getMissingCount() == 1) out.add(r);
        }
        Collections.sort(out, new Comparator<MatchResult>() {
            @Override
            public int compare(MatchResult a, MatchResult b) {
                return a.getRecipe().getName().compareToIgnoreCase(b.getRecipe().getName());
            }
        });
        return out;
    }
}
