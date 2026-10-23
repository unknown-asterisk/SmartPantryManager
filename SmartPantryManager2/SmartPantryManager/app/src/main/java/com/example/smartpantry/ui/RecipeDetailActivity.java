package com.example.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.example.smartpantry.db.DatabaseHelper;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.MatchResult;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;
import com.example.smartpantry.util.Prefs;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

/**
 * Recipe Detail screen. Receives only the recipe's id through an Intent extra, reloads the recipe
 * from SQLite, and shows the full ingredient list (with a tick/cross against the live pantry) and the method.
 * The overflow menu lets the user share the recipe as plain text using an implicit Intent (ACTION_SEND).
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Intent extra: database id of the recipe to display. */
    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        setSupportActionBar((MaterialToolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_recipe_detail);

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        recipe = db.getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MatchResult result = RecipeMatcher.evaluate(recipe,
                RecipeMatcher.buildPantryTotals(db.getAllIngredients(), Prefs.isIgnoreExpired(this)));

        ((TextView) findViewById(R.id.tv_recipe_title)).setText(recipe.getName());
        ((TextView) findViewById(R.id.tv_recipe_desc)).setText(recipe.getDescription());
        bindStatus(result);
        bindIngredients(result);
        bindSteps(recipe);
    }

    /** Adds the three-dot menu (contains "Share recipe"). */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share && recipe != null) {
            shareRecipe();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Builds a plain-text version of the recipe and opens the Android share sheet. */
    private void shareRecipe() {
        StringBuilder sb = new StringBuilder(recipe.getName()).append("\n\nIngredients:\n");
        for (RecipeIngredient ri : recipe.getIngredients()) {
            sb.append("- ").append(ri.toDisplayString()).append("\n");
        }
        sb.append("\nMethod:\n");
        List<String> steps = recipe.getStepList();
        for (int i = 0; i < steps.size(); i++) {
            sb.append(i + 1).append(". ").append(steps.get(i)).append("\n");
        }

        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, recipe.getName());
        send.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(send, "Share recipe"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void bindStatus(MatchResult result) {
        TextView status = findViewById(R.id.tv_status);
        if (result.isStrictMatch()) {
            status.setText("You have everything you need to cook this right now!");
            status.setBackgroundResource(R.drawable.bg_status_ok);
        } else {
            int n = result.getMissingCount();
            status.setText("Missing " + n + (n == 1 ? " ingredient" : " ingredients") + " - not cookable yet.");
            status.setBackgroundResource(R.drawable.bg_status_missing);
        }
    }

    /** One row per required ingredient: green tick if in the pantry, red cross + reason if not. */
    private void bindIngredients(MatchResult result) {
        LinearLayout container = findViewById(R.id.ll_ingredients);
        container.removeAllViews();
        List<MatchResult.IngredientCheck> checks = result.getChecks();
        for (MatchResult.IngredientCheck check : checks) {
            TextView row = new TextView(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            row.setPadding(0, 6, 0, 6);
            row.setTextSize(16);
            String line = check.getIngredient().toDisplayString();
            if (check.isSatisfied()) {
                row.setText("\u2714  " + line);
                row.setTextColor(getColor(R.color.green_700));
            } else {
                row.setText("\u2718  " + line + "  -  " + check.getNote());
                row.setTextColor(getColor(R.color.red_700));
            }
            container.addView(row);
        }
    }

    private void bindSteps(Recipe recipe) {
        StringBuilder sb = new StringBuilder();
        List<String> steps = recipe.getStepList();
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) sb.append("\n\n");
            sb.append(i + 1).append(". ").append(steps.get(i));
        }
        ((TextView) findViewById(R.id.tv_steps)).setText(sb.toString());
    }
}
