package me.crz.minecraft.kingdoms.utils;

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
        if (minutes < 1.0) {
            return UtilMath.trim(seconds) + " Seconds";
        }
        if (hours < 1.0) {
            return UtilMath.trim(minutes) + " Minutes";
        }
        if (days < 1.0) {
            return UtilMath.trim(hours) + " Hours";
        }
        if (months < 1.0) {
            return UtilMath.trim(days) + " Days";
        }
        if (years < 1.0) {
            return - UtilMath.trim(months) + " Months";
        }
        return UtilMath.trim(years) + " Years";
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
