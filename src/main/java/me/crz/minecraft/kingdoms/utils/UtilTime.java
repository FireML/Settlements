package me.crz.minecraft.kingdoms.utils;

import java.text.DecimalFormat;

public class UtilTime {
    public UtilTime() {
        super();
    }

    public static String format(final long milliSeconds) {
        return format((double) milliSeconds / 1000.0);
    }

    public static String format(double seconds) {
        if (seconds < 0.0) {
            seconds = 0.0;
        }
        final double minutes = seconds / 60.0;
        final double hours = minutes / 60.0;
        final double days = hours / 24.0;
        final double months = days / 30.0;
        final double years = days / 365.0;
        DecimalFormat format = new DecimalFormat("#.#");
        if (minutes < 1.0) {
            return format.format(UtilMath.trim(seconds, 1)) + " Second" + plural(seconds);
        }
        if (hours < 1.0) {
            return format.format(UtilMath.trim(minutes, 1)) + " Minute" + plural(minutes);
        }
        if (days < 1.0) {
            return format.format(UtilMath.trim(hours, 1)) + " Hour" + plural(hours);
        }
        if (months < 1.0) {
            return format.format(UtilMath.trim(days, 1)) + " Day" + plural(days);
        }
        if (years < 1.0) {
            return format.format(UtilMath.trim(months, 1)) + " Month" + plural(months);
        }
        return format.format(UtilMath.trim(years, 1)) + " Year" + plural(years);
    }

    private static String plural(double number) {
        return UtilMath.trim(number, 1) == 1 ? "" : "s";
    }

    public static boolean elapsedNano(final long from, final long required) {
        return System.nanoTime() - from > required;
    }

    public enum MinecraftTime {
        SUNRISE(23000),
        DAY(6000),
        SUNSET(13100),
        NIGHT(18000);

        private int ticks;

        MinecraftTime(final int ticks) {
            this.ticks = ticks;
        }

        public int getTicks() {
            return ticks;
        }

        public static MinecraftTime fromString(final String name) {
            MinecraftTime[] values;
            for (int length = (values = values()).length, i = 0; i < length; ++i) {
                final MinecraftTime minecraftTime = values[i];
                if (minecraftTime.name().equalsIgnoreCase(name)) {
                    return minecraftTime;
                }
            }
            return null;
        }

        public static boolean exists(final String name) {
            return fromString(name) != null;
        }
    }
}
