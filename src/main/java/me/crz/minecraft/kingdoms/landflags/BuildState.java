package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.rank.Rankable;
import me.crz.minecraft.kingdoms.utils.NameTag;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum BuildState implements NameTag, Rankable<BuildState> {
    PRIVATE(Material.RED_WOOL, AppliesTo.LAND, AppliesTo.EMPIRE),
    FRIENDS(Material.ORANGE_WOOL, AppliesTo.LAND),
    OFFICERS(Material.LIGHT_BLUE_WOOL, AppliesTo.LAND, AppliesTo.SETTLEMENT),
    RESIDENTS(Material.YELLOW_WOOL, AppliesTo.LAND, AppliesTo.SETTLEMENT),
    ALLY(Material.CYAN_WOOL),
    TRUSTED(Material.BLUE_WOOL, AppliesTo.LAND, AppliesTo.SETTLEMENT),
    PUBLIC(Material.WHITE_WOOL);

    private final Material icon;
    private final Set<AppliesTo> appliesTo;

    BuildState(Material icon, AppliesTo... appliesTo) {
        this.icon = icon;
        if (appliesTo.length > 0) {
            this.appliesTo = EnumSet.noneOf(AppliesTo.class);
            Collections.addAll(this.appliesTo, appliesTo);
        } else {
            this.appliesTo = EnumSet.allOf(AppliesTo.class);
        }
    }

    public static List<BuildState> values(AppliesTo to) {
        return Arrays.stream(values()).filter(v -> v.appliesTo(to)).collect(Collectors.toList());
    }

    public static BuildState valueOf(String s, AppliesTo to) {
        BuildState state = valueOf(s);
        if (state.appliesTo(to)) {
            return state;
        }
        throw new IllegalArgumentException("No enum constant " + s + " applying to " + to + " found!");
    }

    public CItem getItem() {
        return new CItem(icon).hideAllFlags(true);
    }

    public BuildState next(AppliesTo to) {
        List<BuildState> values = values(to);
        if (ordinal() + 1 < values.size()) {
            return values.get(ordinal() + 1);
        }
        return values.get(0);
    }

    public boolean appliesTo(AppliesTo to) {
        return appliesTo.contains(to);
    }

    public enum AppliesTo {
        LAND,
        SETTLEMENT,
        EMPIRE;
    }
}
