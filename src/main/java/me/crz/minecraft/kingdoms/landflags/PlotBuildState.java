package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Next;
import org.bukkit.Material;

public enum PlotBuildState implements NameTag, Next<PlotBuildState> {
    PRIVATE(Material.RED_WOOL),
    FRIENDS(Material.ORANGE_WOOL),
    RESIDENTS(Material.YELLOW_WOOL),
    OFFICERS(Material.LIGHT_BLUE_WOOL),
    ALLY(Material.CYAN_WOOL),
    TRUSTED(Material.BLUE_WOOL),
    PUBLIC(Material.WHITE_WOOL);

    private final Material icon;

    PlotBuildState(Material icon) {
        this.icon = icon;
    }

    public CItem getItem() {
        return new CItem(icon).hideAllFlags(true);
    }

    @Override
    public PlotBuildState[] getValues() {
        return PlotBuildState.values();
    }
}
