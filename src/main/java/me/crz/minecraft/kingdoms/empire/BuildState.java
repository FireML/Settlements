package me.crz.minecraft.kingdoms.empire;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Next;
import org.bukkit.Material;

public enum BuildState implements NameTag, Next<BuildState> {
    PRIVATE(Material.BLUE_WOOL),
    ALLY(Material.CYAN_WOOL),
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
