package com.example.smartpantry.model;

import java.util.ArrayList;
import java.util.List;

/** The outcome of checking ONE recipe against the pantry, ingredient by ingredient. */
public class MatchResult {

    /** Result for a single required ingredient. */
    public static class IngredientCheck {
        private final RecipeIngredient ingredient;
        private final boolean satisfied;
        private final String note;

        public IngredientCheck(RecipeIngredient ingredient, boolean satisfied, String note) {
            this.ingredient = ingredient;
            this.satisfied = satisfied;
            this.note = note;
        }

        public RecipeIngredient getIngredient() { return ingredient; }
        public boolean isSatisfied() { return satisfied; }
        /** Human readable reason, e.g. "not in pantry" or "have 100 g, need 200 g". */
        public String getNote() { return note; }
    }

    private final Recipe recipe;
    private final List<IngredientCheck> checks;
    private int expiringIngredientCount;

    public MatchResult(Recipe recipe, List<IngredientCheck> checks) {
        this.recipe = recipe;
        this.checks = checks;
    }

    public Recipe getRecipe() { return recipe; }
    public List<IngredientCheck> getChecks() { return checks; }
    public int getExpiringIngredientCount() { return expiringIngredientCount; }
    public void setExpiringIngredientCount(int n) { this.expiringIngredientCount = n; }

    public List<IngredientCheck> getMissing() {
        List<IngredientCheck> missing = new ArrayList<>();
        for (IngredientCheck c : checks) {
            if (!c.isSatisfied()) missing.add(c);
        }
        return missing;
    }

    public int getMissingCount() { return getMissing().size(); }

    /** THE strict rule: every single required ingredient is satisfied (and the recipe has ingredients). */
    public boolean isStrictMatch() {
        return !checks.isEmpty() && getMissingCount() == 0;
    }
}
