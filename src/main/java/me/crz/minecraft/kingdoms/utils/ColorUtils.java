package me.crz.minecraft.kingdoms.utils;

/*
 * Copyright (C) 2015 Max Lee (https://github.com/Phoenix616)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License as published by
 * the Mozilla Foundation, version 2.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v2.0 for more details.
 *
 * You should have received a copy of the Mozilla Public License v2.0
 * along with this program. If not, see <http://mozilla.org/MPL/2.0/>.
 */

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for color related operations.
 */
public class ColorUtils {

    static Color[] colors = {
            Color.fromRGB(0, 0, 0),
            Color.fromRGB(0, 0, 170),
            Color.fromRGB(0, 170, 0),
            Color.fromRGB(0, 170, 170),
            Color.fromRGB(170, 0, 0),
            Color.fromRGB(170, 0, 170),
            Color.fromRGB(255, 170, 0),
            Color.fromRGB(170, 170, 170),
            Color.fromRGB(85, 85, 85),
            Color.fromRGB(85, 85, 255),
            Color.fromRGB(85, 255, 85),
            Color.fromRGB(85, 255, 255),
            Color.fromRGB(255, 85, 85),
            Color.fromRGB(255, 85, 255),
            Color.fromRGB(255, 255, 85),
            Color.fromRGB(255, 255, 255)
    };

    static Map<ChatColor, ItemStack> colorHeads = new EnumMap<>(ChatColor.class);

    static {
        addHead(ChatColor.BLACK, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY3YTJmMjE4YTZlNmUzOGYyYjU0NWY2YzE3NzMzZjRlZjliYmIyODhlNzU0MDI5NDljMDUyMTg5ZWUifX19");
        addHead(ChatColor.DARK_BLUE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmE0NjA1MzAxMmM2OGYyODlhYmNmYjE3YWI4MDQyZDVhZmJhOTVkY2FhOTljOTljMWUwMzYwODg2ZDM1In19fQ==");
        addHead(ChatColor.DARK_GREEN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGU5YjI3ZmNjZDgwOTIxYmQyNjNjOTFkYzUxMWQwOWU5YTc0NjU1NWU2YzljYWQ1MmU4NTYyZWQwMTgyYTJmIn19fQ==");
        addHead(ChatColor.DARK_AQUA, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTViOWE0ODQ2N2YwMjEyYWE2ODg2NGU2MzQyMTE2ZjhmNzlhMjc1NDU0YmYyMTVmNjdmNzAxYTZmMmM4MTgifX19");
        addHead(ChatColor.DARK_RED, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGY0ZGMzYzM3NTNiZjViMGI3ZjA4MWNkYjQ5YjgzZDM3NDI4YTEyZTQxODdmNjM0NmRlYzA2ZmFjNTRjZSJ9fX0");
        addHead(ChatColor.DARK_PURPLE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTMyYWUyY2I4ZDJhZTYxNTE0MWQyYzY1ODkyZjM2NGZjYWRkZjczZmRlYzk5YmUxYWE2ODc0ODYzZWViNWMifX19");
        addHead(ChatColor.GOLD, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM0ODg2ZWYzNjJiMmM4MjNhNmFhNjUyNDFjNWM3ZGU3MWM5NGQ4ZWM1ODIyYzUxZTk2OTc2NjQxZjUzZWEzNSJ9fX0=");
        addHead(ChatColor.GRAY, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmQzY2ZjMjM5MDA2YjI1N2I4YjIwZjg1YTdiZjQyMDI2YzRhZGEwODRjMTQ0OGQwNGUwYzQwNmNlOGEyZWEzMSJ9fX0=");
        addHead(ChatColor.DARK_GRAY, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjA4ZjMyMzQ2MmZiNDM0ZTkyOGJkNjcyODYzOGM5NDRlZTNkODEyZTE2MmI5YzZiYTA3MGZjYWM5YmY5In19fQ==");
        addHead(ChatColor.BLUE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWY4NjkwNDhmMDZkMzE4ZTUwNThiY2EwYTg3NmE1OTg2MDc5ZjQ1YTc2NGQxMmFiMzRhNDkzMWRiNmI4MGFkYyJ9fX0=");
        addHead(ChatColor.GREEN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDI3Y2E0NmY2YTliYjg5YTI0ZmNhZjRjYzBhY2Y1ZTgyODVhNjZkYjc1MjEzNzhlZDI5MDlhZTQ0OTY5N2YifX19");
        addHead(ChatColor.AQUA, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMDdjNzhmM2VlNzgzZmVlY2QyNjkyZWJhNTQ4NTFkYTVjNDMyMzA1NWViZDJmNjgzY2QzZTgzMDJmZWE3YyJ9fX0=");
        addHead(ChatColor.RED, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZkZTNiZmNlMmQ4Y2I3MjRkZTg1NTZlNWVjMjFiN2YxNWY1ODQ2ODRhYjc4NTIxNGFkZDE2NGJlNzYyNGIifX19");
        addHead(ChatColor.LIGHT_PURPLE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VmMGM1NzczZGY1NjBjYzNmYzczYjU0YjVmMDhjZDY5ODU2NDE1YWI1NjlhMzdkNmQ0NGYyZjQyM2RmMjAifX19");
        addHead(ChatColor.YELLOW, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY0MTY4MmY0MzYwNmM1YzlhZDI2YmM3ZWE4YTMwZWU0NzU0N2M5ZGZkM2M2Y2RhNDllMWMxYTI4MTZjZjBiYSJ9fX0=");
        addHead(ChatColor.WHITE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY2YTVjOTg5MjhmYTVkNGI1ZDViOGVmYjQ5MDE1NWI0ZGRhMzk1NmJjYWE5MzcxMTc3ODE0NTMyY2ZjIn19fQ==");
    }

    private static void addHead(ChatColor chatColor, String textures) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "ChatColor:" + ChatColor.BLACK.name());
        profile.setProperty(new ProfileProperty("textures", textures));
        meta.setPlayerProfile(profile);
        head.setItemMeta(meta);
        colorHeads.put(chatColor, head);
    }

    /**
     * Get the distance between two colors
     * @param colorA
     * @param colorB
     * @return The distance between the color values.
     */
    private static double getDistance(Color colorA, Color colorB) {
        double mean = (colorA.getRed() + colorB.getRed()) / 2.0;

        double r = colorA.getRed() - colorB.getRed();
        double g = colorA.getGreen() - colorB.getGreen();
        int b = colorA.getBlue() - colorB.getBlue();

        double weightR = 2 + mean / 256.0;
        double weightG = 4.0;
        double weightB = 2 + (255 - mean) / 256.0;

        return weightR * r * r + weightG * g * g + weightB * b * b;
    }

    /**
     * Get the nearest Minecraft ChatColor to a normal Color
     * @param color The color value
     * @return The nearest Minecraft ChatColor
     */
    public static ChatColor getNearestChatColor(Color color) {
        int index = 0;
        int best = -1;
        for (int i = 0; i < colors.length; i++) {
            double distance = getDistance(color, colors[i]);
            if (distance < best || best == -1) {
                best = (int) distance;
                index = i;
            }
        }
        return ChatColor.values()[index];
    }

    public static ItemStack getColorHead(ChatColor chatColor) {
        return colorHeads.getOrDefault(chatColor, new ItemStack(Material.DIRT));
    }

    public static ChatColor getColor(String colorString, ChatColor def) {
        if (colorString != null) {
            try {
                return ChatColor.valueOf(colorString.toUpperCase());
            } catch (IllegalArgumentException e) {
                ChatColor color = null;
                if (colorString.length() == 1) {
                    color = ChatColor.getByChar(colorString);
                } else if (colorString.length() == 2) {
                    color = ChatColor.getByChar(colorString.charAt(1));
                }
                if (color != null) {
                    return color;
                }
            }
        }
        return ChatColor.WHITE;
    }
}
