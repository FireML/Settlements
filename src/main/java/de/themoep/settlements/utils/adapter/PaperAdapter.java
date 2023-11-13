package de.themoep.settlements.utils.adapter;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.title.Title;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class PaperAdapter {

    public void setProfile(SkullMeta skull, UUID uuid, String name, String... properties) {
        PlayerProfile profile = Bukkit.createProfile(uuid, name);
        for (int i = 0; i + 1 < properties.length; i += 2) {
            profile.setProperty(new ProfileProperty(properties[i], properties[i+1]));
        }
        skull.setPlayerProfile(profile);
    }

    public void sendTitle(Player player, Component title, Component subTitle, int in, int stay, int out) {
        Duration inDuration = Duration.ofSeconds(in / 20);
        Duration stayDuration = Duration.ofSeconds(stay / 20);
        Duration outDuration = Duration.ofSeconds(out / 20);
        player.showTitle(Title.title(title, subTitle, Title.Times.times(inDuration, stayDuration, outDuration)));
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
    public ItemStack createBook(String title, String author, List<Component> components) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        List<Component> finalComponents = new ArrayList<>();
        components.forEach(component -> finalComponents.add(ComponentUtils.parseColors(component)));
        book.setItemMeta(createBookMeta((BookMeta) book.getItemMeta(), title, author, finalComponents));
        return book;
    }

    public ItemMeta createBookMeta(BookMeta bookMeta, String title, String author, List<Component> pages) {
        bookMeta.setTitle(title);
        bookMeta.setAuthor(author);

        return (ItemMeta) bookMeta.pages(pages.stream()
                .map(c -> GsonComponentSerializer.builder().build().deserialize(ComponentUtils.toString(ComponentUtils.parseColors(c))))
                .collect(Collectors.toList()));
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
