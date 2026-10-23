package com.example.smartpantry.db;

import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/** The 22 recipes pre-loaded into the database the first time the app runs. */
final class SeedData {

    private SeedData() { }

    private static RecipeIngredient i(String name, double qty, String unit) {
        return new RecipeIngredient(name, qty, unit);
    }

    private static Recipe r(String name, String description, String[] steps, RecipeIngredient... ingredients) {
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < steps.length; n++) {
            if (n > 0) sb.append('\n');
            sb.append(steps[n]);
        }
        Recipe recipe = new Recipe(0, name, description, sb.toString());
        for (RecipeIngredient ri : ingredients) recipe.addIngredient(ri);
        return recipe;
    }

    private static String[] s(String... steps) {
        return steps;
    }

    static List<Recipe> recipes() {
        List<Recipe> list = new ArrayList<>();

        list.add(r("Cheese Omelette", "A fast, protein-packed breakfast.",
                s("Beat the eggs with the salt.", "Melt the butter in a pan over medium heat.",
                        "Pour in the eggs and cook until almost set.", "Add the cheese, fold in half and serve."),
                i("egg", 3, "piece"), i("cheese", 50, "g"), i("butter", 10, "g"), i("salt", 1, "tsp")));

        list.add(r("Tomato Pasta", "Classic pasta with a quick fresh tomato sauce.",
                s("Boil the pasta until al dente.", "Fry crushed garlic in olive oil for 1 minute.",
                        "Add chopped tomatoes and simmer for 10 minutes.", "Toss the pasta through the sauce."),
                i("pasta", 200, "g"), i("tomato", 4, "piece"), i("garlic", 2, "piece"), i("olive oil", 2, "tbsp")));

        list.add(r("Scrambled Eggs on Toast", "Simple, creamy and ready in 10 minutes.",
                s("Toast the bread.", "Melt the butter in a pan on low heat.",
                        "Add the beaten eggs and stir gently until softly set.", "Serve on the toast."),
                i("egg", 2, "piece"), i("bread", 2, "piece"), i("butter", 10, "g")));

        list.add(r("Egg Fried Rice", "A great way to use up cooked-style leftovers.",
                s("Cook the rice and let it cool.", "Stir-fry the diced onion and carrot for 3 minutes.",
                        "Push veg aside, scramble the eggs in the pan.", "Add rice and soy sauce and toss for 3 minutes."),
                i("rice", 200, "g"), i("egg", 2, "piece"), i("carrot", 1, "piece"),
                i("onion", 1, "piece"), i("soy sauce", 2, "tbsp")));

        list.add(r("Fluffy Pancakes", "Weekend breakfast from basic staples.",
                s("Whisk flour, sugar, egg and milk into a smooth batter.", "Heat a little butter in a pan.",
                        "Pour in small ladles of batter.", "Flip when bubbles appear and cook until golden."),
                i("flour", 150, "g"), i("milk", 250, "ml"), i("egg", 1, "piece"),
                i("sugar", 2, "tbsp"), i("butter", 20, "g")));

        list.add(r("Creamy Mashed Potatoes", "Comfort food side dish.",
                s("Peel and cube the potatoes.", "Boil for 15-20 minutes until soft.",
                        "Drain and mash with butter and warm milk.", "Season with salt."),
                i("potato", 500, "g"), i("butter", 30, "g"), i("milk", 50, "ml"), i("salt", 1, "tsp")));

        list.add(r("Garlic Bread", "Crispy, buttery and ready in 10 minutes.",
                s("Mix softened butter with crushed garlic.", "Spread over the bread slices.",
                        "Bake at 200 C for 8-10 minutes until golden."),
                i("bread", 4, "piece"), i("butter", 40, "g"), i("garlic", 2, "piece")));

        list.add(r("Veggie Stir-Fry", "Crunchy vegetables in a savoury soy glaze.",
                s("Slice all the vegetables thinly.", "Heat the oil in a wok until very hot.",
                        "Stir-fry the vegetables for 4-5 minutes.", "Add soy sauce and toss to coat."),
                i("carrot", 1, "piece"), i("bell pepper", 1, "piece"), i("onion", 1, "piece"),
                i("soy sauce", 2, "tbsp"), i("olive oil", 1, "tbsp")));

        list.add(r("Cheese Toastie", "The ultimate quick lunch.",
                s("Butter the outside of each slice of bread.", "Place cheese between the slices.",
                        "Fry on both sides until golden and the cheese melts."),
                i("bread", 2, "piece"), i("cheese", 40, "g"), i("butter", 10, "g")));

        list.add(r("Tuna Pasta", "A pantry-staple dinner.",
                s("Boil the pasta until al dente.", "Soften the chopped onion in olive oil.",
                        "Stir in the drained tuna.", "Toss the pasta through and serve."),
                i("pasta", 200, "g"), i("tuna", 1, "piece"), i("onion", 1, "piece"), i("olive oil", 1, "tbsp")));

        list.add(r("Banana Oat Porridge", "A warm, filling breakfast.",
                s("Simmer the oats in the milk for 5 minutes, stirring.", "Slice the banana on top.",
                        "Drizzle with honey and serve."),
                i("oat", 50, "g"), i("milk", 250, "ml"), i("banana", 1, "piece"), i("honey", 1, "tbsp")));

        list.add(r("Potato and Onion Soup", "Thick, hearty and cheap.",
                s("Soften the chopped onion in butter.", "Add cubed potato and stock; simmer for 20 minutes.",
                        "Blend until smooth.", "Stir in the milk and heat through."),
                i("potato", 400, "g"), i("onion", 1, "piece"), i("butter", 20, "g"),
                i("milk", 200, "ml"), i("stock", 500, "ml")));

        list.add(r("Egg Noodle Stir-Fry", "Ten-minute noodles.",
                s("Cook the noodles and drain.", "Scramble the eggs in a hot wok.",
                        "Add noodles, soy sauce and sliced spring onion.", "Toss for 2 minutes and serve."),
                i("noodle", 200, "g"), i("egg", 2, "piece"), i("soy sauce", 2, "tbsp"), i("spring onion", 2, "piece")));

        list.add(r("Roasted Tomato Soup", "Rich soup from simple ingredients.",
                s("Soften onion and garlic in butter.", "Add chopped tomatoes and stock.",
                        "Simmer for 20 minutes.", "Blend until smooth."),
                i("tomato", 6, "piece"), i("onion", 1, "piece"), i("garlic", 2, "piece"),
                i("butter", 20, "g"), i("stock", 500, "ml")));

        list.add(r("Chicken Rice Bowl", "A balanced one-bowl dinner.",
                s("Cook the rice.", "Fry diced chicken until golden and cooked through.",
                        "Steam the broccoli for 4 minutes.", "Combine in a bowl and drizzle with soy sauce."),
                i("chicken breast", 300, "g"), i("rice", 200, "g"), i("soy sauce", 2, "tbsp"), i("broccoli", 150, "g")));

        list.add(r("Macaroni Cheese", "Baked-style comfort food.",
                s("Boil the macaroni until tender.", "Melt butter, stir in flour, then whisk in milk to make a sauce.",
                        "Stir in the cheese until melted.", "Combine with the macaroni and serve."),
                i("macaroni", 200, "g"), i("cheese", 150, "g"), i("milk", 250, "ml"),
                i("butter", 30, "g"), i("flour", 20, "g")));

        list.add(r("French Toast", "Turn stale bread into breakfast.",
                s("Whisk the eggs, milk and sugar.", "Soak the bread slices for 30 seconds each side.",
                        "Fry in butter until golden brown."),
                i("bread", 4, "piece"), i("egg", 2, "piece"), i("milk", 100, "ml"),
                i("sugar", 1, "tbsp"), i("butter", 15, "g")));

        list.add(r("Quick Guacamole", "Fresh dip in five minutes.",
                s("Mash the avocados in a bowl.", "Stir in diced tomato and onion.",
                        "Squeeze in the lemon juice and season with salt."),
                i("avocado", 2, "piece"), i("tomato", 1, "piece"), i("onion", 1, "piece"),
                i("lemon", 1, "piece"), i("salt", 1, "tsp")));

        list.add(r("Red Lentil Curry", "Budget-friendly, filling and vegan.",
                s("Fry the onion and garlic until soft.", "Add the curry powder and cook for 1 minute.",
                        "Add lentils, tomatoes and coconut milk.", "Simmer for 20 minutes until thick."),
                i("lentil", 200, "g"), i("onion", 1, "piece"), i("tomato", 2, "piece"), i("garlic", 2, "piece"),
                i("curry powder", 2, "tsp"), i("coconut milk", 400, "ml")));

        list.add(r("Bean and Cheese Burrito", "Hearty handheld meal.",
                s("Warm the beans with the chopped onion.", "Spoon onto the tortillas and top with cheese.",
                        "Roll up and toast in a dry pan until crisp."),
                i("tortilla", 2, "piece"), i("bean", 200, "g"), i("cheese", 50, "g"), i("onion", 1, "piece")));

        list.add(r("Banana Yoghurt Smoothie", "Cool and creamy drink.",
                s("Peel the banana.", "Blend everything until smooth.", "Serve immediately."),
                i("banana", 1, "piece"), i("milk", 250, "ml"), i("yoghurt", 100, "g"), i("honey", 1, "tbsp")));

        list.add(r("Loaded Baked Potato", "Crispy skin, melty cheese.",
                s("Prick the potatoes and bake at 200 C for 60 minutes.", "Split open and add butter.",
                        "Top with cheese and a pinch of salt."),
                i("potato", 2, "piece"), i("cheese", 50, "g"), i("butter", 10, "g"), i("salt", 1, "tsp")));

                list.add(r("Cheese and Tomato Sandwich", "A quick lunch.",
                s("Butter the bread.", "Slice the tomato and cheese.", "Layer them between the slices."),
                i("bread", 2, "piece"), i("cheese", 40, "g"), i("tomato", 1, "piece"), i("butter", 10, "g")));

        list.add(r("Garlic Butter Pasta", "Simple and savoury.",
                s("Boil the pasta until al dente.", "Melt the butter and fry the crushed garlic.", "Toss the pasta through."),
                i("pasta", 200, "g"), i("butter", 40, "g"), i("garlic", 3, "piece")));

        list.add(r("Rice and Beans", "Filling and cheap.",
                s("Cook the rice.", "Fry the onion and garlic in oil.", "Add the beans, heat through and serve over the rice."),
                i("rice", 200, "g"), i("bean", 200, "g"), i("onion", 1, "piece"),
                i("garlic", 2, "piece"), i("olive oil", 1, "tbsp")));

        return list;
    }
}
