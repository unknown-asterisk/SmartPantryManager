package com.example.smartpantry.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Thin wrapper around SharedPreferences for the Settings screen. */
public final class Prefs {

    private static final String FILE = "smart_pantry_prefs";
    private static final String KEY_ALERTS = "expiry_alerts_enabled";
    private static final String KEY_ALERT_DAYS = "expiry_alert_days";
    private static final String KEY_IGNORE_EXPIRED = "ignore_expired";
    private static final String KEY_DEFAULT_UNIT = "default_unit";

    private Prefs() { }

    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean isExpiryAlertsEnabled(Context c) { return sp(c).getBoolean(KEY_ALERTS, true); }
    public static void setExpiryAlertsEnabled(Context c, boolean v) { sp(c).edit().putBoolean(KEY_ALERTS, v).apply(); }

    public static int getAlertDays(Context c) { return sp(c).getInt(KEY_ALERT_DAYS, 3); }
    public static void setAlertDays(Context c, int days) { sp(c).edit().putInt(KEY_ALERT_DAYS, days).apply(); }

    /** When true, expired pantry items are not used for recipe matching. */
    public static boolean isIgnoreExpired(Context c) { return sp(c).getBoolean(KEY_IGNORE_EXPIRED, true); }
    public static void setIgnoreExpired(Context c, boolean v) { sp(c).edit().putBoolean(KEY_IGNORE_EXPIRED, v).apply(); }

    public static String getDefaultUnit(Context c) { return sp(c).getString(KEY_DEFAULT_UNIT, "g"); }
    public static void setDefaultUnit(Context c, String unit) { sp(c).edit().putString(KEY_DEFAULT_UNIT, unit).apply(); }
}
