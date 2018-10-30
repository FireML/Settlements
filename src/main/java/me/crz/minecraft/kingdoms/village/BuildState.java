package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.utils.NameTag;
import org.bukkit.Material;

public enum BuildState implements NameTag {
    RESIDENTS(Material.YELLOW_WOOL),
    OFFICERS(Material.LIGHT_BLUE_WOOL),
    ALLY(Material.WHITE_WOOL),
    PUBLIC(Material.LIME_WOOL);

    private final Material icon;

    BuildState(Material icon) {
        this.icon = icon;
    }

    public static BuildState next(final BuildState current) {
        if (current.ordinal() + 1 >= values().length) {
            return values()[0];
        }
        return values()[current.ordinal() + 1];
    }

    public CItem getItem() {
        return new CItem(icon).hideAllFlags(true);
    }
}
