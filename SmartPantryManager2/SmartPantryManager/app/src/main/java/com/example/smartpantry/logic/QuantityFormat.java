package com.example.smartpantry.logic;

import java.util.Locale;

/** Formats quantities without ugly trailing zeros: 2.0 -> "2", 0.50 -> "0.5". */
public final class QuantityFormat {

    private QuantityFormat() { }

    public static String format(double q) {
        if (Math.abs(q - Math.rint(q)) < 1e-9) {
            return String.valueOf((long) Math.rint(q));
        }
        String s = String.format(Locale.US, "%.2f", q);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return s;
    }
}
