package de.themoep.settlements.utils;

import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UtilMath {
    public UtilMath() {
        super();
    }

    public static double trim(final double d) {
        return trim(d, 1);
    }

    public static double trim(double d, final int degree) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            d = 0.0;
        }
        return Math.round(d * Math.pow(10, degree)) / Math.pow(10, degree);
    }

    public static boolean parseInt(final String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static double getKDR(final int kills, final int deaths) {
        if (kills == 0 || deaths == 0) {
            return 0.0;
        }
        return trim((double) kills / (double) deaths, 2);
    }

    public static String formatWithComas(final int number) {
        String string = String.valueOf(number);
        if (string.length() <= 4) {
            return string;
        }
        for (int i = string.length() - 3; i > 0; i -= 3) {
            string = string.substring(0, i) + "," + string.substring(i);
        }
        return string;
    }

    public static int floor(final double d1) {
        final int i = (int) d1;
        return (d1 >= i) ? i : (i - 1);
    }

    public static int d(final float f1) {
        final int i = (int) f1;
        return (f1 >= i) ? i : (i - 1);
    }

    public static List<Block> getDiscreteCircle(final Block centre, final double radius) {
        final List<Block> blocks = new ArrayList<>();
        for (double x = radius, y = 0.0, radiusSquared = radius * radius; x > y; x = Math.sqrt(x * x - 2.0 * y - 1.0), y = Math.sqrt(radiusSquared - x * x)) {
            final int x2 = (int) Math.round(x);
            final int y2 = (int) Math.round(y);
            blocks.add(centre.getWorld().getBlockAt(centre.getX() + x2, centre.getY(), centre.getZ() + y2));
            blocks.add(centre.getWorld().getBlockAt(centre.getX() - x2, centre.getY(), centre.getZ() + y2));
            blocks.add(centre.getWorld().getBlockAt(centre.getX() + x2, centre.getY(), centre.getZ() - y2));
            blocks.add(centre.getWorld().getBlockAt(centre.getX() - x2, centre.getY(), centre.getZ() - y2));
            blocks.add(centre.getWorld().getBlockAt(centre.getX() + y2, centre.getY(), centre.getZ() + x2));
            blocks.add(centre.getWorld().getBlockAt(centre.getX() - y2, centre.getY(), centre.getZ() + x2));
            blocks.add(centre.getWorld().getBlockAt(centre.getX() + y2, centre.getY(), centre.getZ() - x2));
            blocks.add(centre.getWorld().getBlockAt(centre.getX() - y2, centre.getY(), centre.getZ() - x2));
        }
        return blocks;
    }

    public static int getRandom(final int lower, final int upper) {
        final Random random = new Random();
        return random.nextInt(upper - lower + 1) + lower;
    }
}
