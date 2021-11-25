package de.themoep.settlements.land;

import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.NameTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class UseFlag implements NameTag {

    private final Collection<Use> useTypes;
    private final Material icon;

    private final String name;
    private final BuildState defaultState;

    public UseFlag(String name, BuildState defaultState, Material icon, Collection<Use> useTypes) {
        this.name = name;
        this.defaultState = defaultState;
        this.icon = icon;
        this.useTypes = useTypes;
    }

    public ItemStack getItem() {
        return ItemUtils.toIcon(getIcon());
    }

    public Material getIcon() {
        return icon;
    }

    public Collection<Use> getUseTypes() {
        return useTypes;
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
        return "use." + name().toLowerCase().replace('_', '-');
    }
}
