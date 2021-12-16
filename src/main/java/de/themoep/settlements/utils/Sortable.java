package de.themoep.settlements.utils;

import org.bukkit.inventory.ItemStack;

import java.util.Comparator;

public interface Sortable<T> extends ReplacementProvider, ConfigKey {

    Sort<T>[] getSorts();

    ItemStack getIcon();

    interface Sort<T> extends NameTag, Next<Sort<T>> {

        Comparator<T> getComparator();
    }

}
