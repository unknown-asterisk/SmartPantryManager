package com.example.smartpantry.ui;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Shared behaviour for the three top-level screens (Pantry, Recipes, Settings):
 * wires up the bottom navigation bar and uses explicit Intents to switch screens.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /** Call from onCreate() after setContentView(). */
    protected void setupBottomNav(final int selectedItemId) {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(selectedItemId);               // highlight this screen's tab
        nav.setOnItemSelectedListener(item -> {              // set AFTER selecting, so it doesn't fire
            int id = item.getItemId();
            if (id != selectedItemId) {
                navigateToTab(id);
            }
            return false; // never change the highlighted tab here; the new Activity highlights its own
        });
    }

    private void navigateToTab(int itemId) {
        if (itemId == R.id.nav_pantry) {
            // Pantry is the root screen: bring the existing instance back and drop everything above it.
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else {
            Class<?> target = (itemId == R.id.nav_recipes)
                    ? SuggestedRecipesActivity.class
                    : SettingsActivity.class;
            startActivity(new Intent(this, target));
            if (!(this instanceof MainActivity)) {
                finish(); // keep the back stack as Pantry -> one other tab
            }
        }
        overridePendingTransition(0, 0);
    }
}
