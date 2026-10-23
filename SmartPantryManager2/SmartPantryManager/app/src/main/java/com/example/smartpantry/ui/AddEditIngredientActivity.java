package com.example.smartpantry.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.example.smartpantry.db.DatabaseHelper;
import com.example.smartpantry.logic.IngredientNormalizer;
import com.example.smartpantry.logic.QuantityFormat;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.util.DateUtils;
import com.example.smartpantry.util.Prefs;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

/**
 * Add / Edit Ingredient screen (one Activity, two modes).
 *  - Started with no extra            -> CREATE a new pantry item
 *  - Started with EXTRA_INGREDIENT_ID -> UPDATE that item (fields are pre-filled)
 * All input is validated before anything is written to the database.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Intent extra: the database id of the ingredient being edited. Absent = add mode. */
    public static final String EXTRA_INGREDIENT_ID = "extra_ingredient_id";

    private static final double MAX_QUANTITY = 100000;

    private DatabaseHelper db;
    private TextInputLayout nameLayout, quantityLayout;
    private TextInputEditText nameInput, quantityInput, expiryInput;
    private Spinner unitSpinner;

    private long editingId = -1;
    private String expiryIso = null; // yyyy-MM-dd, null = no expiry date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = DatabaseHelper.getInstance(this);

        nameLayout = findViewById(R.id.layout_name);
        quantityLayout = findViewById(R.id.layout_quantity);
        nameInput = findViewById(R.id.et_name);
        quantityInput = findViewById(R.id.et_quantity);
        expiryInput = findViewById(R.id.et_expiry);
        unitSpinner = findViewById(R.id.spinner_unit);

        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this, R.array.units, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);

        editingId = getIntent().getLongExtra(EXTRA_INGREDIENT_ID, -1);
        if (editingId != -1) {
            setTitle(R.string.title_edit_ingredient);
            populateForEdit();
        } else {
            setTitle(R.string.title_add_ingredient);
            selectUnit(Prefs.getDefaultUnit(this));
        }

        expiryInput.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btn_clear_date).setOnClickListener(v -> {
            expiryIso = null;
            showExpiry();
        });
        findViewById(R.id.btn_save).setOnClickListener(v -> attemptSave());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /** Edit mode: load the row from SQLite and fill the form. */
    private void populateForEdit() {
        Ingredient existing = db.getIngredient(editingId);
        if (existing == null) { // deleted in the meantime
            Toast.makeText(this, "That ingredient no longer exists", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        nameInput.setText(existing.getName());
        quantityInput.setText(QuantityFormat.format(existing.getQuantity()));
        selectUnit(existing.getUnit());
        expiryIso = existing.getExpiryDate();
        showExpiry();
    }

    private void selectUnit(String unit) {
        String[] units = getResources().getStringArray(R.array.units);
        for (int i = 0; i < units.length; i++) {
            if (units[i].equalsIgnoreCase(unit)) {
                unitSpinner.setSelection(i);
                return;
            }
        }
    }

    private void showExpiry() {
        expiryInput.setText(expiryIso == null ? "" : DateUtils.toDisplay(expiryIso));
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (expiryIso != null) {
            try {
                String[] p = expiryIso.split("-");
                cal.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
            } catch (RuntimeException ignored) {
                // keep today's date if the stored value is somehow malformed
            }
        }
        new DatePickerDialog(this, (view, year, month, day) -> {
            expiryIso = DateUtils.toIso(year, month, day);
            showExpiry();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private static String textOf(TextInputEditText field) {
        Editable e = field.getText();
        return e == null ? "" : e.toString();
    }

    /** INPUT VALIDATION, then CREATE or UPDATE. */
    private void attemptSave() {
        nameLayout.setError(null);
        quantityLayout.setError(null);

        String name = textOf(nameInput).trim().replaceAll("\\s+", " ");
        String qtyText = textOf(quantityInput).trim();
        boolean valid = true;

        // --- name: required, max 40 chars, must contain letters (not just digits/symbols)
        if (name.isEmpty()) {
            nameLayout.setError("Please enter an ingredient name");
            valid = false;
        } else if (IngredientNormalizer.normalize(name).isEmpty()) {
            nameLayout.setError("The name must contain letters");
            valid = false;
        } else if (name.length() > 40) {
            nameLayout.setError("Name is too long (max 40 characters)");
            valid = false;
        }

        // --- quantity: required, a valid number, greater than 0, not absurdly large
        double quantity = 0;
        if (qtyText.isEmpty()) {
            quantityLayout.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(qtyText.replace(',', '.'));
                if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity <= 0) {
                    quantityLayout.setError("Quantity must be greater than 0");
                    valid = false;
                } else if (quantity > MAX_QUANTITY) {
                    quantityLayout.setError("Quantity is too large");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                quantityLayout.setError("Enter a valid number, e.g. 2 or 0.5");
                valid = false;
            }
        }

        if (!valid) return; // stay on the form so the user can fix the errors

        String unit = (String) unitSpinner.getSelectedItem();
        Ingredient ingredient = new Ingredient(editingId == -1 ? 0 : editingId, name, quantity, unit, expiryIso);

        if (editingId == -1) {
            long newId = db.addIngredient(ingredient);                    // CREATE
            if (newId == -1) {
                Toast.makeText(this, "Could not save. Please try again.", Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, name + " added to your pantry", Toast.LENGTH_SHORT).show();
        } else {
            int rows = db.updateIngredient(ingredient);                   // UPDATE
            if (rows == 0) {
                Toast.makeText(this, "Could not update. Please try again.", Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, name + " updated", Toast.LENGTH_SHORT).show();
        }
        finish(); // MainActivity.onResume() reloads the list
    }
}
