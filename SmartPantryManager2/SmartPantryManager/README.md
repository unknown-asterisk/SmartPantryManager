# Smart Pantry Manager

A native **Java** Android app (Android Studio) that helps reduce food waste. The user records the ingredients they
already have at home (their *pantry*) and the app suggests **only the recipes they can cook right now** - every
ingredient present, in at least the required quantity. No shopping trip needed.

*Mobile App Development 700 - Practical Assignment (Richfield, Faculty of IT).*

## Features
- **Pantry management** - add, edit and delete ingredients (name, quantity, unit, optional expiry date) with input validation.
- **Pantry list** - `RecyclerView` + custom `PantryAdapter`, bound to SQLite, with expired / expiring-soon highlighting and "Undo" on delete.
- **22 pre-loaded recipes** seeded into the database on first run.
- **Suggested Recipes** - runs the strict-matching rule. Shows *only* fully-cookable recipes ("Cook now" tab).
  Friendly message when nothing matches.
- **Almost there (bonus)** - separate tab for recipes missing exactly **one** ingredient (never mixed with the strict list).
- **Recipe detail** - full ingredient list (tick/cross against the live pantry) and numbered method.
- **Settings** - expiring-soon alerts on/off, alert window (1/3/5/7 days), ignore expired items when matching, default unit, clear pantry.
- **Bottom navigation** between Pantry / Recipes / Settings.
- **Uses food that is about to expire first**: recipes that use soon-to-expire ingredients are sorted to the top.

**Out of scope (deliberately not used):** Google Maps, any mapping SDK, GPS/location, payments, Play Store publishing.
The manifest declares **no permissions at all**.

## Database choice: SQLite (`SQLiteOpenHelper`)
SQLite is built into Android, runs fully on-device, needs no account/server/internet, and persists automatically between
app launches. The data is relational (recipes -> recipe ingredients), and the strict-matching rule needs fast local
reads, so a local relational database is the simplest correct fit. A cloud option (Firebase / PostgreSQL) would add a
network dependency and a backend with no benefit for a single-user pantry.

**Schema**

| Table | Columns |
|---|---|
| `pantry` | `_id` PK, `name`, `quantity` REAL, `unit`, `expiry_date` (nullable, `yyyy-MM-dd`) |
| `recipes` | `_id` PK, `name`, `description`, `steps` (one per line) |
| `recipe_ingredients` | `_id` PK, `recipe_id` FK -> `recipes` (ON DELETE CASCADE), `name`, `quantity`, `unit` |

CRUD on `pantry`: `addIngredient` (Create), `getAllIngredients` / `getIngredient` (Read), `updateIngredient` (Update),
`deleteIngredient` / `deleteAllIngredients` (Delete). Data lives in the app's private storage, so it survives closing and reopening the app.

## The strict-matching rule (core logic)
Implemented in [`logic/RecipeMatcher.java`](app/src/main/java/com/example/smartpantry/logic/RecipeMatcher.java):

1. **Normalise names** (`IngredientNormalizer`): lower-case, remove filler words ("fresh", "large"), singularise
   (`tomatoes -> tomato`, `berries -> berry`, `Eggs -> egg`) and map simple synonyms (`scallion -> spring onion`, `capsicum -> bell pepper`).
2. **Convert units** (`UnitConverter`) to a base: kg/g/oz/lb -> grams; l/ml/tsp/tbsp/cup -> millilitres; pieces/slices/cloves/cans -> count.
   So `1 kg` in the pantry satisfies `500 g` in a recipe.
3. **Sum duplicate pantry entries** (`Eggs 2` + `egg 4` = 6 eggs).
4. **Check every recipe ingredient**; the recipe is suggested **only if nothing is missing or short**.
   Needs 5 ingredients, pantry has 4 -> **not suggested**.
5. Incompatible units (e.g. pantry has `500 g` of egg, recipe needs `2 pieces`) are *not guessed* - treated as missing, with an explanation on the detail screen.

The logic is plain Java (no Android classes) and is covered by JUnit tests: `app/src/test/.../RecipeMatcherTest.java`
(run with *Right-click -> Run 'RecipeMatcherTest'*, or `./gradlew test`).

## Project structure
```
app/src/main/java/com/example/smartpantry/
  model/   Ingredient, Recipe, RecipeIngredient, MatchResult
  db/      DatabaseHelper (SQLite CRUD + seeding), SeedData (22 recipes)
  logic/   RecipeMatcher, IngredientNormalizer, UnitConverter, QuantityFormat
  ui/      MainActivity (pantry list), AddEditIngredientActivity, SuggestedRecipesActivity,
           RecipeDetailActivity, SettingsActivity, BaseActivity (bottom nav), PantryAdapter, RecipeAdapter
  util/    DateUtils, Prefs (SharedPreferences)
app/src/main/res/  layouts, menu, drawables (vector icons), strings, theme
app/src/test/      RecipeMatcherTest (JUnit)
```
Screens (5): Pantry List -> Add/Edit Ingredient; Suggested Recipes -> Recipe Detail; Settings.
Intents carry data with the extras `EXTRA_INGREDIENT_ID` and `EXTRA_RECIPE_ID`.

## Setup / run
1. Install **Android Studio** (Koala / Ladybug or newer, bundled JDK 17).
2. **File -> Open** and select the `SmartPantryManager` folder. Let Gradle sync finish
   (it downloads Gradle 8.7 / Android Gradle Plugin 8.5.2 automatically; internet needed for first sync).
   If Studio offers to update the Gradle plugin, either accept or ignore.
3. Create / start an emulator (API 24+; e.g. Pixel 6, API 34) or plug in a phone with USB debugging.
4. Press **Run**. First launch creates the SQLite database and seeds the 22 recipes.

Requirements: minSdk 24, compile/target SDK 34, Java 8 source level, no Kotlin.

## Quick demo script (matches the video structure)
1. Pantry is empty -> Recipes tab shows *"Your pantry is empty"*.
2. Add `Egg 3 piece`, `Cheese 100 g`, `Butter 200 g` -> *Cook now* is still empty (Omelette also needs salt) -> *Almost there* lists **Cheese Omelette** (missing salt).
3. Add `Salt 1 tsp` -> **Cheese Omelette** appears in *Cook now*. Delete the salt -> it disappears again.
4. Add `Tomatoes`/`tomato`, `Eggs` vs `egg`, `1 kg` vs `500 g` to show plural/unit robustness.
5. Edit an item, delete an item (Undo), show validation errors (empty name, quantity 0, `abc`).
6. Close the app completely and reopen -> data still present.
