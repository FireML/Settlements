package de.themoep.settlements.land;

import de.themoep.settlements.utils.NameTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum PlotMode implements NameTag {
    pvp(Material.IRON_SWORD),
    mobSpawning(Material.SKELETON_SKULL),
    passiveSpawning(Material.PIG_SPAWN_EGG, true),
    fireSpread(Material.FIREWORK_STAR),
    explosions(Material.TNT),
    buff(Material.POTION, true),
    embassy(Material.RED_BANNER, AppliesTo.LAND);
    private final Material icon;
    private final Set<AppliesTo> appliesTo;
    private final boolean defaultValue;

    PlotMode(Material icon, AppliesTo... appliesTo) {
        this(icon, false, appliesTo);
    }

    PlotMode(Material icon, boolean defaultValue, AppliesTo... appliesTo) {
        this.icon = icon;
        this.defaultValue = defaultValue;
        if (appliesTo.length > 0) {
            this.appliesTo = EnumSet.noneOf(AppliesTo.class);
            Collections.addAll(this.appliesTo, appliesTo);
        } else {
            this.appliesTo = EnumSet.allOf(AppliesTo.class);
        }
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }

    public boolean set(boolean b, ModeAble modeAble) {
        return modeAble.setMode(this, b);
    }

    public boolean get(ModeAble modeAble) {
        return modeAble.getMode(this);
    }

    public boolean appliesTo(AppliesTo to) {
        return appliesTo.contains(to);
    }

    public static List<PlotMode> values(AppliesTo to) {
        return Arrays.stream(values()).filter(v -> v.appliesTo(to)).toList();
    }

    public static PlotMode get(String s, AppliesTo to) {
        PlotMode state = get(s);
        if (state.appliesTo(to)) {
            return state;
        }
        throw new IllegalArgumentException("No enum constant " + s + " applying to " + to + " found!");
    }

    private static PlotMode get(String s) {
        try {
            return valueOf(s);
        } catch (IllegalArgumentException e) {
            for (PlotMode mode : values()) {
                if (mode.name().equalsIgnoreCase(s)) {
                    return mode;
                }
            }
        }
        throw new IllegalArgumentException("No PlotMode with name " + s + " found!");
    }

    public boolean getDefault() {
        return defaultValue;
    }

    public enum AppliesTo {
        LAND,
        LAND_OWNER
    }
}
