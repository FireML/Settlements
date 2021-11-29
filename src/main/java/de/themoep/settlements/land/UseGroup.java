package de.themoep.settlements.land;

import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.NameTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class UseGroup implements NameTag {

    private final Collection<UseFlag> useFlags;
    private final Material icon;

    private final String name;
    private final BuildState defaultState;

    public UseGroup(String name, BuildState defaultState, Material icon, Collection<UseFlag> useFlags) {
        this.name = name;
        this.defaultState = defaultState;
        this.icon = icon;
        this.useFlags = useFlags;
    }

    public ItemStack getItem() {
        return ItemUtils.toIcon(icon);
    }

    public Collection<UseFlag> getUseFlags() {
        return useFlags;
    }

    public BuildState getDefault() {
        return defaultState;
    }

    public BuildState getDefault(BuildState defaultState) {
        return getDefault() != null ? getDefault() : defaultState;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String getKey() {
        return "use-group." + name().toLowerCase().replace('_', '-');
    }
}
