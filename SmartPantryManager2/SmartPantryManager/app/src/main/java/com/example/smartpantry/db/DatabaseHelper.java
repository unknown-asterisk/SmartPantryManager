package com.example.smartpantry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Local SQLite database (SQLiteOpenHelper).
 *
 * Tables
 *  - pantry             : the user's ingredients                (full CRUD from the UI)
 *  - recipes            : seeded on first run                   (read-only from the UI)
 *  - recipe_ingredients : the ingredients each recipe requires  (FK -> recipes, ON DELETE CASCADE)
 *
 * SQLite stores the data in the app's private storage, so it survives closing/reopening the app.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    // table + column names
    private static final String T_PANTRY = "pantry";
    private static final String T_RECIPES = "recipes";
    private static final String T_RECIPE_ING = "recipe_ingredients";

    private static final String COL_ID = "_id";
    private static final String COL_NAME = "name";
    private static final String COL_QTY = "quantity";
    private static final String COL_UNIT = "unit";
    private static final String COL_EXPIRY = "expiry_date";
    private static final String COL_DESC = "description";
    private static final String COL_STEPS = "steps";
    private static final String COL_RECIPE_ID = "recipe_id";

    private static DatabaseHelper instance;

    /** Single shared instance (avoids "database is locked" errors from many open helpers). */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /** Runs only the very first time the database file is created. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_QTY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + COL_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + T_RECIPES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_DESC + " TEXT, "
                + COL_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_RECIPE_ING + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_ID + " INTEGER NOT NULL REFERENCES " + T_RECIPES + "(" + COL_ID + ") ON DELETE CASCADE, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_QTY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL)");

        seedRecipes(db);
    }

    /** Version 1 only: a future schema change would add a proper migration here. */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    private void seedRecipes(SQLiteDatabase db) {
        for (Recipe recipe : SeedData.recipes()) {
            ContentValues rv = new ContentValues();
            rv.put(COL_NAME, recipe.getName());
            rv.put(COL_DESC, recipe.getDescription());
            rv.put(COL_STEPS, recipe.getSteps());
            long recipeId = db.insert(T_RECIPES, null, rv);

            for (RecipeIngredient ri : recipe.getIngredients()) {
                ContentValues iv = new ContentValues();
                iv.put(COL_RECIPE_ID, recipeId);
                iv.put(COL_NAME, ri.getName());
                iv.put(COL_QTY, ri.getQuantity());
                iv.put(COL_UNIT, ri.getUnit());
                db.insert(T_RECIPE_ING, null, iv);
            }
        }
    }

    // ------------------------------------------------------------------ PANTRY: CREATE

    /** @return the new row id, or -1 on failure */
    public long addIngredient(Ingredient ingredient) {
        return getWritableDatabase().insert(T_PANTRY, null, toValues(ingredient));
    }

    // ------------------------------------------------------------------ PANTRY: READ

    public List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, null, null, null, null,
                COL_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                list.add(fromCursor(c));
            }
        }
        return list;
    }

    /** @return the ingredient, or null if no row has that id */
    public Ingredient getIngredient(long id) {
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    // ------------------------------------------------------------------ PANTRY: UPDATE

    /** @return number of rows updated (1 on success) */
    public int updateIngredient(Ingredient ingredient) {
        return getWritableDatabase().update(T_PANTRY, toValues(ingredient), COL_ID + "=?",
                new String[]{String.valueOf(ingredient.getId())});
    }

    // ------------------------------------------------------------------ PANTRY: DELETE

    /** @return number of rows deleted (1 on success) */
    public int deleteIngredient(long id) {
        return getWritableDatabase().delete(T_PANTRY, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int deleteAllIngredients() {
        return getWritableDatabase().delete(T_PANTRY, null, null);
    }

    private ContentValues toValues(Ingredient i) {
        ContentValues v = new ContentValues();
        v.put(COL_NAME, i.getName());
        v.put(COL_QTY, i.getQuantity());
        v.put(COL_UNIT, i.getUnit());
        v.put(COL_EXPIRY, i.getExpiryDate()); // null is stored as SQL NULL
        return v;
    }

    private Ingredient fromCursor(Cursor c) {
        int expiryIdx = c.getColumnIndexOrThrow(COL_EXPIRY);
        return new Ingredient(
                c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(COL_QTY)),
                c.getString(c.getColumnIndexOrThrow(COL_UNIT)),
                c.isNull(expiryIdx) ? null : c.getString(expiryIdx));
    }

    // ------------------------------------------------------------------ RECIPES (read only)

    /** All recipes, each with its full ingredient list (2 queries, not 1 per recipe). */
    public List<Recipe> getAllRecipes() {
        Map<Long, Recipe> map = new LinkedHashMap<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor c = db.query(T_RECIPES, null, null, null, null, null, COL_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                Recipe r = recipeFromCursor(c);
                map.put(r.getId(), r);
            }
        }
        try (Cursor c = db.query(T_RECIPE_ING, null, null, null, null, null, COL_ID + " ASC")) {
            while (c.moveToNext()) {
                Recipe r = map.get(c.getLong(c.getColumnIndexOrThrow(COL_RECIPE_ID)));
                if (r != null) r.addIngredient(recipeIngredientFromCursor(c));
            }
        }
        return new ArrayList<>(map.values());
    }

    /** @return the recipe with its ingredients, or null if not found */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Recipe recipe = null;
        try (Cursor c = db.query(T_RECIPES, null, COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (c.moveToFirst()) recipe = recipeFromCursor(c);
        }
        if (recipe == null) return null;

        try (Cursor c = db.query(T_RECIPE_ING, null, COL_RECIPE_ID + "=?", new String[]{String.valueOf(id)},
                null, null, COL_ID + " ASC")) {
            while (c.moveToNext()) recipe.addIngredient(recipeIngredientFromCursor(c));
        }
        return recipe;
    }

    private Recipe recipeFromCursor(Cursor c) {
        return new Recipe(
                c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                c.getString(c.getColumnIndexOrThrow(COL_DESC)),
                c.getString(c.getColumnIndexOrThrow(COL_STEPS)));
    }

    private RecipeIngredient recipeIngredientFromCursor(Cursor c) {
        return new RecipeIngredient(
                c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(COL_QTY)),
                c.getString(c.getColumnIndexOrThrow(COL_UNIT)));
    }
}
