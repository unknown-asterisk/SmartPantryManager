package com.example.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.db.DatabaseHelper;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.util.Prefs;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * Pantry List screen (launcher). READ: lists every ingredient from SQLite in a RecyclerView.
 * Also the entry point for CREATE (FAB), UPDATE (tap/edit button) and DELETE (bin button).
 *
 * Lifecycle note: the list is reloaded in onResume(), so it is always fresh after returning
 * from the Add/Edit screen or from Settings (e.g. after "Clear pantry").
 */
public class MainActivity extends BaseActivity implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyView;
    private TextView bannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((MaterialToolbar) findViewById(R.id.toolbar));
        setTitle(R.string.title_pantry);

        db = DatabaseHelper.getInstance(this);

        recyclerView = findViewById(R.id.rv_pantry);
        emptyView = findViewById(R.id.empty_view);
        bannerView = findViewById(R.id.tv_expiry_banner);

        adapter = new PantryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditIngredientActivity.class)));

        setupBottomNav(R.id.nav_pantry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        List<Ingredient> items = db.getAllIngredients();

        boolean alertsOn = Prefs.isExpiryAlertsEnabled(this);
        int window = Prefs.getAlertDays(this);
        adapter.setAlertWindowDays(alertsOn ? window : -1);
        adapter.setItems(items);

        boolean empty = items.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);

        updateExpiryBanner(items, alertsOn, window);
    }

    private void updateExpiryBanner(List<Ingredient> items, boolean alertsOn, int window) {
        if (!alertsOn) {
            bannerView.setVisibility(View.GONE);
            return;
        }
        int expiring = 0;
        int expired = 0;
        for (Ingredient item : items) {
            Long days = item.daysUntilExpiry();
            if (days == null) continue;
            if (days < 0) expired++;
            else if (days <= window) expiring++;
        }
        if (expiring + expired == 0) {
            bannerView.setVisibility(View.GONE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (expiring > 0) {
            sb.append(expiring).append(expiring == 1 ? " item expires" : " items expire")
                    .append(" within ").append(window).append(window == 1 ? " day" : " days");
        }
        if (expiring > 0 && expired > 0) sb.append("  |  ");
        if (expired > 0) sb.append(expired).append(" already expired");
        bannerView.setText(sb.toString());
        bannerView.setVisibility(View.VISIBLE);
    }

    // ---- PantryAdapter.Listener -------------------------------------------------------------

    @Override
    public void onEdit(Ingredient ingredient) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_INGREDIENT_ID, ingredient.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(final Ingredient ingredient) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + ingredient.getName() + " from your pantry?")
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deleteIngredient(ingredient.getId());
                    loadPantry();
                    showUndoSnackbar(ingredient);
                })
                .show();
    }

    private void showUndoSnackbar(final Ingredient deleted) {
        Snackbar.make(recyclerView, deleted.getName() + " deleted", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.bottom_nav)
                .setAction("Undo", v -> {
                    Ingredient copy = new Ingredient(0, deleted.getName(), deleted.getQuantity(),
                            deleted.getUnit(), deleted.getExpiryDate());
                    db.addIngredient(copy);
                    loadPantry();
                })
                .show();
    }
}
