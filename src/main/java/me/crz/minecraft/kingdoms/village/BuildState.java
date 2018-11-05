package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Next;
import org.bukkit.Material;

public enum BuildState implements NameTag, Next<BuildState> {
    RESIDENTS(Material.YELLOW_WOOL),
    OFFICERS(Material.LIGHT_BLUE_WOOL),
    ALLY(Material.CYAN_WOOL),
    TRUSTED(Material.BLUE_WOOL),
    PUBLIC(Material.WHITE_WOOL);

    private final Material icon;

    BuildState(Material icon) {
        this.icon = icon;
    }

    public CItem getItem() {
        return new CItem(icon).hideAllFlags(true);
    }

    @Override
    public BuildState[] getValues() {
        return BuildState.values();
    }
}
