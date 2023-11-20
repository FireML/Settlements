package de.themoep.settlements.utils.adapter;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
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
import uk.firedev.poleislib.utils.ComponentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class PaperAdapter {

    public void setProfile(SkullMeta skull, UUID uuid, String name, String... properties) {
        PlayerProfile profile = Bukkit.createProfile(uuid, name);
        for (int i = 0; i + 1 < properties.length; i += 2) {
            profile.setProperty(new ProfileProperty(properties[i], properties[i+1]));
        }
        skull.setPlayerProfile(profile);
    }

    public InventoryHolder getHolder(Inventory inventory) {
        return inventory.getHolder(false);
    }

    public void markOwned(Item item, Player player) {
        item.setPickupDelay(0);
        item.setOwner(player.getUniqueId());
        item.setCanMobPickup(false);
    }

    public CommandMap getCommandMap() {
        return Bukkit.getServer().getCommandMap();
    }

    /**
     * Create a written book
     * @param title         The book title
     * @param author        The book author
     * @param components    The book components
     * @return The book
     */
    public Book createBook(Component title, Component author, List<Component> pages) {
        return Book.book(title, author, pages);
    }

    public CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
        return entity.teleportAsync(location);
    }

    public CompletionStage<Chunk> getChunkAtAsync(Location location) {
        return location.getWorld().getChunkAtAsync(location);
    }

    public CompletionStage<Chunk> getChunkAtAsync(World world, int x, int z) {
        return world.getChunkAtAsync(x, z);
    }

    public BlockState getBlockState(Block block, boolean snapshot) {
        return block.getState(snapshot);
    }
}
