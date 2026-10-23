package com.example.smartpantry.model;

import java.util.ArrayList;
import java.util.List;

/** A recipe with its required ingredients and preparation steps. */
public class Recipe {
    private final long id;
    private final String name;
    private final String description;
    private final String steps; // one step per line
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(long id, String name, String description, String steps) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.steps = steps;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSteps() { return steps; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
    }

    /** Steps split into a list (one per line, blank lines ignored). */
    public List<String> getStepList() {
        List<String> list = new ArrayList<>();
        if (steps == null) return list;
        for (String s : steps.split("\n")) {
            if (!s.trim().isEmpty()) list.add(s.trim());
        }
        return list;
    }
}
