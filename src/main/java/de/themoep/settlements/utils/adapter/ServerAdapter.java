package de.themoep.settlements.utils.adapter;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public interface ServerAdapter {

    /**
     * Set profile values of a player head
     * @param skull         The head's meta
     * @param uuid          The player's UUID
     * @param name          The player's name
     * @param properties    Additional properties
     */
    void setProfile(SkullMeta skull, UUID uuid, String name, String... properties);

    /**
     * Send a title to a player
     * @param player    The palyer
     * @param title     The title components
     * @param subTitle  The subtitle components
     * @param in        The fade in time
     * @param stay      The stay time
     * @param out       The fade out time
     */
    void sendTitle(Player player, BaseComponent[] title, BaseComponent[] subTitle, int in, int stay, int out);

    /**
     * Get an InventoryHolder form an Inventory without potentially creating a BlockStat snapshot
     * @param inventory The inventory
     * @return The InventoryHolder
     */
    InventoryHolder getHolder(Inventory inventory);

    /**
     * Try to mark a dropped item owned by a player
     * @param item      The item
     * @param player    The player
     */
    void markOwned(Item item, Player player);

    /**
     * Get the server's command map
     * @return The command map
     */
    CommandMap getCommandMap();
}
