package de.themoep.settlements.utils;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class UtilTime {
    private static final DecimalFormat SECONDS_FORMAT = new DecimalFormat("#.#");

    public static String format(final long milliSeconds) {
        return format(TimeUnit.MILLISECONDS.toSeconds(milliSeconds));
    }

    public static String format(final long milliSeconds, boolean one) {
        return format(TimeUnit.MILLISECONDS.toSeconds(milliSeconds), true);
    }

    public static String format(double seconds) {
        return format(seconds, true);
    }

    public static String format(double seconds, boolean includeNext) {
        if (seconds < 0.0) {
            seconds = 0.0;
        }

        int minutes = (int) (seconds / 60.0);
        if (minutes < 1.0) {
            double trimedSeconds = UtilMath.trim(seconds, 1);
            return SECONDS_FORMAT.format(trimedSeconds) + " Second" + plural(trimedSeconds);
        }

        int restSeconds = (int) (seconds % 60);
        int hours = (int) (minutes / 60.0);
        if (hours < 1.0) {
            return minutes + " Minute" + plural(minutes) + " " + (includeNext && restSeconds > 0 ? restSeconds + " Second" + plural(restSeconds) : "");
        }

        int restMinutes = minutes % 60;
        int days = (int) (hours / 24.0);
        if (days < 1.0) {
            return hours + " Hour" + plural(hours) + " " + (includeNext && restMinutes > 0 ? restMinutes + " Minute" + plural(restMinutes) : "");
        }

        int restHours = hours % 24;
        int months = (int) (days / 30.0);
        if (months < 1.0) {
            return days + " Day" + plural(days) + " " + (includeNext && restHours > 0 ? restHours + " Hour" + plural(restHours) : "");
        }

        int restDays = days % 30;
        int years = (int) (days / 365.0);
        if (years < 1.0) {
            return months + " Month" + plural(months) + " " + (includeNext && restDays > 0 ? restDays + " Day" + plural(restDays) : "");
        }

        int restMonths = months % 12;
        return years + " Year" + plural(years) + " " + (includeNext && restMonths > 0 ? restMonths + " Month" + plural(restMonths) : "");
    }

    private static String plural(double number) {
        return number == 1 ? "" : "s";
    }

    public static boolean elapsedNano(final long from, final long required) {
        return System.nanoTime() - from > required;
    }

    public enum MinecraftTime implements NameTag, Next<MinecraftTime> {
        MORNING(0),
        DAY(6000),
        DUSK(12000),
        NIGHT(18000);

        private int ticks;

        MinecraftTime(final int ticks) {
            this.ticks = ticks;
        }

        public int getTicks() {
            return ticks;
        }

        public static MinecraftTime getNext(long ticks) {
            for (MinecraftTime time : values()) {
                if (ticks < time.getTicks()) {
                    return time;
                }
            }
            return MORNING;
        }

        public static MinecraftTime getClosest(long ticks) {
            return values()[Math.round(ticks / 6000) % values().length];
        }

        @Override
        public MinecraftTime[] getValues() {
            return MinecraftTime.values();
        }
    }
}
