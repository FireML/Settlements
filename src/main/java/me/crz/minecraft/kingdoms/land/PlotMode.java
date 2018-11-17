package me.crz.minecraft.kingdoms.land;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.utils.NameTag;
import org.bukkit.Material;

public enum PlotMode implements NameTag {
    pvp(Material.IRON_SWORD),
    mobSpawning(Material.SKELETON_SKULL),
    fireSpread(Material.FIREWORK_STAR),
    explosions(Material.TNT),
    buff(Material.POTION, true),
    embassy(Material.RED_BANNER, AppliesTo.LAND);

    private static Map<String, Boolean> DEFAULT_OPTIONS = new HashMap<>();
    static {
        DEFAULT_OPTIONS.put("on", true);
        DEFAULT_OPTIONS.put("off", false);
        DEFAULT_OPTIONS.put("yes", true);
        DEFAULT_OPTIONS.put("no", false);
        DEFAULT_OPTIONS.put("true", true);
        DEFAULT_OPTIONS.put("false", false);
    }
    private Material icon;
    private final Set<AppliesTo> appliesTo;
    private boolean defaultValue;

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

    public CItem getItem() {
        return new CItem(icon).hideAllFlags(true);
    }

    public boolean set(boolean b, ModeAble modeAble) {
        return modeAble.setMode(this, b);
    }

    public boolean get(ModeAble modeAble) {
        return modeAble.getMode(this);
    }

    public Map<String, Boolean> getOptions() {
        return DEFAULT_OPTIONS;
    }

    public boolean appliesTo(AppliesTo to) {
        return appliesTo.contains(to);
    }

    public static List<PlotMode> values(AppliesTo to) {
        return Arrays.stream(values()).filter(v -> v.appliesTo(to)).collect(Collectors.toList());
    }

    public static PlotMode valueOf(String s, AppliesTo to) {
        PlotMode state = valueOf(s);
        if (state.appliesTo(to)) {
            return state;
        }
        throw new IllegalArgumentException("No enum constant " + s + " applying to " + to + " found!");
    }

    public boolean getDefault() {
        return defaultValue;
    }

    public enum AppliesTo {
        LAND,
        LAND_OWNER;
    }
}
