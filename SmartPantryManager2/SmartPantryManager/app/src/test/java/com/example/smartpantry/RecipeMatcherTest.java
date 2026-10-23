package com.example.smartpantry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantry.logic.IngredientNormalizer;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.model.MatchResult;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Local JUnit tests for the strict-matching rule (no emulator needed). */
public class RecipeMatcherTest {

    private static Ingredient pantry(String name, double qty, String unit) {
        return new Ingredient(0, name, qty, unit, null);
    }

    private static Recipe recipe(String name, RecipeIngredient... items) {
        Recipe r = new Recipe(1, name, "", "step");
        for (RecipeIngredient i : items) r.addIngredient(i);
        return r;
    }

    private static List<MatchResult> strict(Recipe recipe, Ingredient... items) {
        List<MatchResult> all = RecipeMatcher.evaluateAll(
                Collections.singletonList(recipe), Arrays.asList(items), true, 3);
        return RecipeMatcher.strictMatches(all);
    }

    @Test
    public void needsFiveHasFour_isNotSuggested() {
        Recipe r = recipe("Five", new RecipeIngredient("a", 1, "piece"), new RecipeIngredient("b", 1, "piece"),
                new RecipeIngredient("c", 1, "piece"), new RecipeIngredient("d", 1, "piece"),
                new RecipeIngredient("e", 1, "piece"));
        assertTrue(strict(r, pantry("a", 1, "piece"), pantry("b", 1, "piece"),
                pantry("c", 1, "piece"), pantry("d", 1, "piece")).isEmpty());
    }

    @Test
    public void hasEverything_isSuggested() {
        Recipe r = recipe("Toast", new RecipeIngredient("bread", 2, "piece"), new RecipeIngredient("butter", 10, "g"));
        assertEquals(1, strict(r, pantry("bread", 4, "piece"), pantry("butter", 250, "g")).size());
    }

    @Test
    public void singularPluralAndCaseAreIgnored() {
        Recipe r = recipe("Salsa", new RecipeIngredient("tomato", 2, "piece"));
        assertEquals(1, strict(r, pantry("  TOMATOES ", 3, "pieces")).size());
        assertEquals("potato", IngredientNormalizer.normalize("Potatoes"));
        assertEquals("berry", IngredientNormalizer.normalize("berries"));
        assertEquals(IngredientNormalizer.normalize("cookie"), IngredientNormalizer.normalize("cookies"));
        assertEquals("hummus", IngredientNormalizer.normalize("hummus"));
        assertEquals("kiwi", IngredientNormalizer.normalize("kiwis"));
        assertEquals("chili", IngredientNormalizer.normalize("Chilis"));
        assertEquals("cherry tomato", IngredientNormalizer.normalize("Cherry Tomatoes"));
        assertEquals("spring onion", IngredientNormalizer.normalize("scallions"));
    }

    @Test
    public void unitsAreConverted() {
        Recipe r = recipe("Mash", new RecipeIngredient("potato", 500, "g"), new RecipeIngredient("milk", 50, "ml"));
        assertEquals(1, strict(r, pantry("potatoes", 1, "kg"), pantry("milk", 1, "l")).size());
    }

    @Test
    public void insufficientQuantity_isNotSuggested() {
        Recipe r = recipe("Pasta", new RecipeIngredient("pasta", 200, "g"));
        assertTrue(strict(r, pantry("pasta", 150, "g")).isEmpty());
    }

    @Test
    public void duplicatePantryEntriesAreSummed() {
        Recipe r = recipe("Omelette", new RecipeIngredient("egg", 6, "piece"));
        assertEquals(1, strict(r, pantry("egg", 2, "piece"), pantry("Eggs", 4, "piece")).size());
    }

    @Test
    public void incompatibleUnitsAreNotGuessed() {
        Recipe r = recipe("Eggs", new RecipeIngredient("egg", 2, "piece"));
        assertTrue(strict(r, pantry("egg", 500, "g")).isEmpty());
    }

    @Test
    public void almostThere_listsOnlyOneMissing_andNeverLeaksIntoStrict() {
        Recipe r = recipe("Two", new RecipeIngredient("rice", 100, "g"), new RecipeIngredient("egg", 1, "piece"));
        List<MatchResult> all = RecipeMatcher.evaluateAll(Collections.singletonList(r),
                Collections.singletonList(pantry("rice", 500, "g")), true, 3);
        assertTrue(RecipeMatcher.strictMatches(all).isEmpty());
        assertEquals(1, RecipeMatcher.almostThere(all).size());
    }

    @Test
    public void expiredItemsAreIgnoredWhenSettingIsOn() {
        Recipe r = recipe("Toast", new RecipeIngredient("bread", 1, "piece"));
        Ingredient old = new Ingredient(0, "bread", 2, "piece", "2000-01-01");
        assertTrue(RecipeMatcher.strictMatches(RecipeMatcher.evaluateAll(
                Collections.singletonList(r), Collections.singletonList(old), true, 3)).isEmpty());
        assertFalse(RecipeMatcher.strictMatches(RecipeMatcher.evaluateAll(
                Collections.singletonList(r), Collections.singletonList(old), false, 3)).isEmpty());
    }

    @Test
    public void emptyPantry_suggestsNothing() {
        Recipe r = recipe("Toast", new RecipeIngredient("bread", 1, "piece"));
        assertTrue(strict(r, new Ingredient[0]).isEmpty());
        assertEquals(0, new ArrayList<MatchResult>().size());
    }
}
