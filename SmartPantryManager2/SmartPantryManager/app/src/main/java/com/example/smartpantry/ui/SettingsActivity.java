package com.example.smartpantry.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.smartpantry.R;
import com.example.smartpantry.db.DatabaseHelper;
import com.example.smartpantry.util.Prefs;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

/** Settings screen: values are stored in SharedPreferences and used by the other screens. */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setSupportActionBar((MaterialToolbar) findViewById(R.id.toolbar));
        setTitle(R.string.title_settings);

        setupAlertSwitch();
        setupAlertWindowSpinner();
        setupIgnoreExpiredSwitch();
        setupDefaultUnitSpinner();
        setupClearPantry();

        setupBottomNav(R.id.nav_settings);
    }

    private void setupAlertSwitch() {
        MaterialSwitch alerts = findViewById(R.id.switch_alerts);
        alerts.setChecked(Prefs.isExpiryAlertsEnabled(this));
        alerts.setOnCheckedChangeListener((button, checked) -> {
            Prefs.setExpiryAlertsEnabled(this, checked);
            findViewById(R.id.spinner_alert_days).setEnabled(checked);
        });
        findViewById(R.id.spinner_alert_days).setEnabled(alerts.isChecked());
    }

    private void setupAlertWindowSpinner() {
        final Spinner spinner = findViewById(R.id.spinner_alert_days);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.alert_days_labels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        final int[] values = getResources().getIntArray(R.array.alert_days_values);
        int current = Prefs.getAlertDays(this);
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) spinner.setSelection(i);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Prefs.setAlertDays(SettingsActivity.this, values[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setupIgnoreExpiredSwitch() {
        MaterialSwitch sw = findViewById(R.id.switch_ignore_expired);
        sw.setChecked(Prefs.isIgnoreExpired(this));
        sw.setOnCheckedChangeListener((button, checked) -> Prefs.setIgnoreExpired(this, checked));
    }

    private void setupDefaultUnitSpinner() {
        final Spinner spinner = findViewById(R.id.spinner_default_unit);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.units, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        final String[] units = getResources().getStringArray(R.array.units);
        String current = Prefs.getDefaultUnit(this);
        for (int i = 0; i < units.length; i++) {
            if (units[i].equals(current)) spinner.setSelection(i);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Prefs.setDefaultUnit(SettingsActivity.this, units[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setupClearPantry() {
        findViewById(R.id.btn_clear_pantry).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Clear whole pantry?")
                        .setMessage("This permanently deletes every ingredient in your pantry.")
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton("Clear", (dialog, which) -> {
                            int n = DatabaseHelper.getInstance(this).deleteAllIngredients();
                            Toast.makeText(this, n + " item(s) removed", Toast.LENGTH_SHORT).show();
                        })
                        .show());
    }
}
