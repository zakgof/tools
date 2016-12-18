package com.zakgof.tools;

public class Utils {

    public static String shorten(String orig, int limit) {
        if (orig.length() < limit)
            return orig;
        return orig.substring(0, limit - 3) + "...";
    }

    public static String fixfloat(float value, int digits) {
//    if (value == null)
//      return "?";
        return String.format(String.format("%%.%df", digits), value);
    }

    public static String weight(float value) {
        if (value == 0)
            return "?";
        if (Math.abs(value - Math.floor(value)) < 0.01)
            return String.format("%.0f", Math.floor(value));
        return String.format("%.1f", value);
    }

    public static Float parseFloat(String s) {
        if (s == null)
            return null;
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
        }
        try {
            return Float.parseFloat(s.replace(',', '.'));
        } catch (NumberFormatException e) {
        }
        return null;
    }
}
