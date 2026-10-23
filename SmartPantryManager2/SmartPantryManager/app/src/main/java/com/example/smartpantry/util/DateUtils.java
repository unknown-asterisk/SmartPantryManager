package com.example.smartpantry.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Small helpers for the ISO (yyyy-MM-dd) expiry dates stored in the database. */
public final class DateUtils {

    public static final String ISO = "yyyy-MM-dd";

    private DateUtils() { }

    /** Whole days from {@code today} until {@code iso}. Negative = already expired. Null if no/invalid date. */
    public static Long daysUntil(String iso, Date today) {
        if (iso == null || iso.trim().isEmpty()) return null;
        try {
            SimpleDateFormat f = new SimpleDateFormat(ISO, Locale.US);
            f.setLenient(false);
            Date target = f.parse(iso.trim());
            Date midnightToday = f.parse(f.format(today)); // strip the time-of-day
            return Math.round((target.getTime() - midnightToday.getTime()) / 86400000.0);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Long daysUntil(String iso) {
        return daysUntil(iso, new Date());
    }

    /** Builds an ISO string from DatePicker values (month is 0-based). */
    public static String toIso(int year, int month0, int day) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month0 + 1, day);
    }

    /** "2026-11-05" -> "05 Nov 2026" for display. */
    public static String toDisplay(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            Date d = new SimpleDateFormat(ISO, Locale.US).parse(iso);
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(d);
        } catch (ParseException e) {
            return iso;
        }
    }
}
