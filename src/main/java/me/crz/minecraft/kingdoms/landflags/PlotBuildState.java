package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.utils.NameTag;
import org.bukkit.Material;

public enum PlotBuildState implements NameTag {
    PRIVATE(Material.RED_WOOL),
    FRIENDS(Material.ORANGE_WOOL),
    RESIDENTS(Material.YELLOW_WOOL),
    OFFICERS(Material.LIGHT_BLUE_WOOL),
    ALLY(Material.WHITE_WOOL),
    PUBLIC(Material.LIME_WOOL);

    private final Material icon;

    PlotBuildState(Material icon) {
        this.icon = icon;
    }

    public static PlotBuildState next(final PlotBuildState current) {
        switch (current) {
            case ALLY:
                return PUBLIC;
            case PUBLIC:
                return PRIVATE;
            case PRIVATE:
                return FRIENDS;
            case FRIENDS:
                return RESIDENTS;
            case RESIDENTS:
                return OFFICERS;
            case OFFICERS:
                return ALLY;
        }
        return RESIDENTS;
    }

    public CItem getItem() {
        return new CItem(icon).hideAllFlags(true);
    }
}
