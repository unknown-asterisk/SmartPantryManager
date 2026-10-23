package com.example.smartpantry.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns messy ingredient names into one comparable "key" so that
 * "Tomatoes", "tomato", " TOMATO " and "fresh tomatoes" all match each other.
 *
 * Both the pantry name and the recipe name go through the SAME function, so the
 * result only has to be consistent, not linguistically perfect.
 */
public final class IngredientNormalizer {

    /** Words that don't change which ingredient it is. */
    private static final Set<String> FILLER = new HashSet<>(Arrays.asList(
            "fresh", "large", "small", "medium", "big", "ripe", "chopped", "diced", "sliced",
            "minced", "grated", "organic", "raw", "of", "a", "an", "the"));

    /** Words that already end in s/us but are NOT plurals. */
    private static final Set<String> NOT_PLURAL = new HashSet<>(Arrays.asList(
            "hummus", "couscous", "asparagus", "molasses", "swiss", "citrus", "grits", "bass"));

    private static final Map<String, String> IRREGULAR = new HashMap<>();
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("halves", "half");
        IRREGULAR.put("knives", "knife");

        // applied AFTER singularising, so keys are singular
        SYNONYMS.put("scallion", "spring onion");
        SYNONYMS.put("green onion", "spring onion");
        SYNONYMS.put("capsicum", "bell pepper");
        SYNONYMS.put("red pepper", "bell pepper");
        SYNONYMS.put("green pepper", "bell pepper");
        SYNONYMS.put("yogurt", "yoghurt");
        SYNONYMS.put("aubergine", "eggplant");
        SYNONYMS.put("courgette", "zucchini");
        SYNONYMS.put("cilantro", "coriander");
        SYNONYMS.put("soya sauce", "soy sauce");
        SYNONYMS.put("plain flour", "flour");
        SYNONYMS.put("all purpose flour", "flour");
        SYNONYMS.put("whole milk", "milk");
        SYNONYMS.put("full cream milk", "milk");
        SYNONYMS.put("rolled oat", "oat");
        SYNONYMS.put("porridge oat", "oat");
        SYNONYMS.put("extra virgin olive oil", "olive oil");
        SYNONYMS.put("cheddar", "cheese");
        SYNONYMS.put("cheddar cheese", "cheese");
        SYNONYMS.put("spaghetti", "pasta");
        SYNONYMS.put("penne", "pasta");
        SYNONYMS.put("fusilli", "pasta");
        SYNONYMS.put("basmati rice", "rice");
        SYNONYMS.put("white rice", "rice");
        SYNONYMS.put("jasmine rice", "rice");
        SYNONYMS.put("long grain rice", "rice");
        SYNONYMS.put("canned tuna", "tuna");
        SYNONYMS.put("tinned tuna", "tuna");
        SYNONYMS.put("kidney bean", "bean");
        SYNONYMS.put("black bean", "bean");
        SYNONYMS.put("pinto bean", "bean");
        SYNONYMS.put("garbanzo bean", "chickpea");
    }

    private IngredientNormalizer() { }

    /** Returns the comparison key ("" if the text has no letters at all). */
    public static String normalize(String raw) {
        if (raw == null) return "";
        String cleaned = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\s]", " ")   // drop digits, punctuation, hyphens
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isEmpty()) return "";

        List<String> words = new ArrayList<>();
        for (String w : cleaned.split(" ")) {
            if (!FILLER.contains(w)) words.add(w);
        }
        if (words.isEmpty()) return "";

        int last = words.size() - 1;
        words.set(last, singularize(words.get(last))); // "cherry tomatoes" -> "cherry tomato"

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(words.get(i));
        }
        String joined = sb.toString();
        String synonym = SYNONYMS.get(joined);
        return synonym != null ? synonym : joined;
    }

    /** Light-weight English singulariser (no NLP library needed). */
    static String singularize(String word) {
        if (word.length() <= 3) return word;
        if (IRREGULAR.containsKey(word)) return IRREGULAR.get(word);
        if (NOT_PLURAL.contains(word)) return word;

        String w = word;
        if (w.endsWith("ies") && w.length() > 4) {
            w = w.substring(0, w.length() - 3) + "y";          // berries -> berry
        } else if (w.endsWith("oes")) {
            w = w.substring(0, w.length() - 2);                // tomatoes -> tomato
        } else if (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("xes")) {
            w = w.substring(0, w.length() - 2);                // peaches -> peach
        } else if (w.endsWith("ss") || w.endsWith("us")) {
            // already singular (watercress, hummus, ...)
        } else if (w.endsWith("s")) {
            w = w.substring(0, w.length() - 1);                // eggs -> egg
        }
        // "cookie" and "cookies" must end up identical -> both become "cooky"
        if (w.length() > 3 && w.endsWith("ie")) {
            w = w.substring(0, w.length() - 2) + "y";
        }
        return w;
    }
}
