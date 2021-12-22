package de.themoep.settlements.utils.adapter;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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

    /**
     * Create a written book
     * @param title         The book title
     * @param author        The book author
     * @param components    The book components
     * @return The book
     */
    default ItemStack createBook(String title, String author, List<BaseComponent[]> components) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        book.setItemMeta(createBookMeta((BookMeta) book.getItemMeta(), title, author, components));
        return book;
    }

    /**
     * Modify a book meta with components
     * @param bookMeta      The book meta to modify
     * @param title         The book title
     * @param author        The book author
     * @param pages         The book pages
     * @return The book
     */
    ItemMeta createBookMeta(BookMeta bookMeta, String title, String author, List<BaseComponent[]> pages);

    /**
     * Teleport somewhere, async if possible
     * @param entity    The entity to teleport
     * @param location  The location
     * @return A completable future for when the teleport was finished (or not)
     */
    CompletableFuture<Boolean> teleportAsync(Entity entity, Location location);

    /**
     * Get a chunk, async if possible
     * @param location  The location to get the chunk at
     * @return A completable future for when the chunk is loaded (or not)
     */
    CompletionStage<Chunk> getChunkAtAsync(Location location);

    /**
     * Get a chunk, async if possible
     * @param world The world
     * @param x     Chunk x coordinate
     * @param z     Chunk z coordinate
     * @return A completable future for when the chunk is loaded (or not)
     */
    CompletionStage<Chunk> getChunkAtAsync(World world, int x, int z);

    /**
     * Get a block state, optionally without creating a snapshot
     * @param block     The block to get the state of
     * @param snapshot  Whether to create a snapshot copy
     * @return The state
     */
    BlockState getBlockState(Block block, boolean snapshot);
}
