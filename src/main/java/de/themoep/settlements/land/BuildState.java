package de.themoep.settlements.land;

import de.themoep.settlements.rank.Rankable;
import de.themoep.settlements.utils.NameTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import uk.firedev.poleislib.utils.ItemUtils;

import java.util.*;
import java.util.stream.Collectors;

public enum BuildState implements NameTag, Rankable {
    PRIVATE(Material.RED_WOOL, AppliesTo.LAND, AppliesTo.EMPIRE),
    MEMBERS(Material.PINK_WOOL, AppliesTo.LAND),
    FRIENDS(Material.ORANGE_WOOL, AppliesTo.LAND),
    OFFICERS(Material.LIGHT_BLUE_WOOL),
    RESIDENTS(Material.YELLOW_WOOL, AppliesTo.LAND, AppliesTo.SETTLEMENT),
    ALLY(Material.CYAN_WOOL),
    TRUSTED(Material.BLUE_WOOL),
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

    public ItemStack getItem() {
        return ItemUtils.toIcon(icon);
    }

    public BuildState next(AppliesTo to) {
        List<BuildState> values = values(to);
        int ordinal = values.indexOf(this);
        if (ordinal + 1 < values.size()) {
            return values.get(ordinal + 1);
        }
        return values.get(0);
    }

    public BuildState previous(AppliesTo to) {
        List<BuildState> values = values(to);
        int ordinal = values.indexOf(this);
        if (ordinal > 0) {
            if (ordinal > values.size()) {
                ordinal = 1;
            }
            return values.get(ordinal - 1);
        }
        return values.get(values.size() - 1);
    }

    public boolean appliesTo(AppliesTo to) {
        return appliesTo.contains(to);
    }

    public enum AppliesTo {
        LAND,
        SETTLEMENT,
        EMPIRE;

        public static AppliesTo get(Object o) {
            if (o instanceof FlagAble flagAble) {
                return flagAble.getBuildStateType();
            }
            return null;
        }
    }
}
