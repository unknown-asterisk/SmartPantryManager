package com.example.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.db.DatabaseHelper;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.model.MatchResult;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.util.Prefs;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggested Recipes screen. Runs the STRICT-MATCHING rule (RecipeMatcher) against the current pantry.
 *
 *  Tab 1 "Cook now"      - ONLY recipes where every ingredient is in the pantry in enough quantity.
 *  Tab 2 "Almost there"  - (bonus) recipes missing exactly one ingredient, clearly separated.
 *
 * The lists are recomputed in onResume(), so adding/removing a pantry item immediately changes the result.
 */
public class SuggestedRecipesActivity extends BaseActivity implements RecipeAdapter.Listener {

    private RecipeAdapter adapter;
    private TabLayout tabs;
    private TextView summaryView;
    private TextView emptyView;
    private RecyclerView recyclerView;

    private List<MatchResult> strictList = new ArrayList<>();
    private List<MatchResult> almostList = new ArrayList<>();
    private boolean pantryIsEmpty = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        setSupportActionBar((MaterialToolbar) findViewById(R.id.toolbar));
        setTitle(R.string.title_recipes);

        tabs = findViewById(R.id.tabs);
        summaryView = findViewById(R.id.tv_summary);
        emptyView = findViewById(R.id.empty_view);
        recyclerView = findViewById(R.id.rv_recipes);

        tabs.addTab(tabs.newTab().setText(R.string.tab_cook_now));
        tabs.addTab(tabs.newTab().setText(R.string.tab_almost));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { showSelectedTab(); }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        adapter = new RecipeAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setupBottomNav(R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        computeMatches();
        showSelectedTab();
    }

    /** Loads pantry + recipes from SQLite and applies the strict-matching rule. */
    private void computeMatches() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        List<Recipe> recipes = db.getAllRecipes();
        List<Ingredient> pantry = db.getAllIngredients();
        pantryIsEmpty = pantry.isEmpty();

        List<MatchResult> all = RecipeMatcher.evaluateAll(
                recipes, pantry, Prefs.isIgnoreExpired(this), Prefs.getAlertDays(this));
        strictList = RecipeMatcher.strictMatches(all);
        almostList = RecipeMatcher.almostThere(all);

        TabLayout.Tab t0 = tabs.getTabAt(0);
        TabLayout.Tab t1 = tabs.getTabAt(1);
        if (t0 != null) t0.setText(getString(R.string.tab_cook_now) + " (" + strictList.size() + ")");
        if (t1 != null) t1.setText(getString(R.string.tab_almost) + " (" + almostList.size() + ")");
    }

    private void showSelectedTab() {
        boolean almostTab = tabs.getSelectedTabPosition() == 1;
        List<MatchResult> list = almostTab ? almostList : strictList;
        adapter.setItems(list, almostTab);

        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            summaryView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            if (almostTab) {
                emptyView.setText("No recipes are missing just one ingredient right now.");
            } else if (pantryIsEmpty) {
                emptyView.setText("Your pantry is empty.\nAdd some ingredients to see recipes.");
            } else {
                // The required "zero matches" feedback message (assignment section 2.2)
                emptyView.setText("No recipes match your pantry yet - add more ingredients.");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            summaryView.setVisibility(View.VISIBLE);
            if (almostTab) {
                summaryView.setText("These are NOT cookable yet - each is missing exactly one ingredient.");
            } else {
                summaryView.setText(list.size() + (list.size() == 1 ? " recipe" : " recipes")
                        + " you can cook right now with what you already have.");
            }
        }
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
