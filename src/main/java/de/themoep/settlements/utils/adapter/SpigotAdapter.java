package de.themoep.settlements.utils.adapter;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
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

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class SpigotAdapter implements ServerAdapter {

    private Field profileField = null;
    private CommandMap commandMap;

    public SpigotAdapter() {
        SkullMeta skullMeta = (SkullMeta) new ItemStack(Material.PLAYER_HEAD).getItemMeta();
        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (CommandMap) f.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setProfile(SkullMeta skull, UUID uuid, String name, String... properties) {
        if (profileField != null) {
            try {
                GameProfile profile = new GameProfile(uuid, name);
                for (int i = 0; i + 1 < properties.length; i += 2) {
                    profile.getProperties().put(properties[i], new Property(properties[i], properties[i+1]));
                }
                profileField.set(skull, new GameProfile(uuid, name));
                return;
            } catch (IllegalAccessException ignored) {}
        }
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        skull.setOwningPlayer(p);
    }

    @Override
    public void sendTitle(Player player, BaseComponent[] title, BaseComponent[] subTitle, int in, int stay, int out) {
        player.sendTitle(
                TextComponent.toLegacyText(title),
                TextComponent.toLegacyText(subTitle),
                in, stay, out
        );
    }

    @Override
    public InventoryHolder getHolder(Inventory inventory) {
        return inventory.getHolder();
    }

    @Override
    public void markOwned(Item item, Player player) {
        item.setPickupDelay(0);
    }

    @Override
    public CommandMap getCommandMap() {
        return commandMap;
    }

    @Override
    public ItemMeta createBookMeta(BookMeta bookMeta, String title, String author, List<BaseComponent[]> pages) {
        bookMeta.setTitle(title);
        bookMeta.setAuthor(author);
        bookMeta.setPages(pages.stream().map(TextComponent::toLegacyText).collect(Collectors.toList()));
        return bookMeta;
    }

    @Override
    public CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
        return CompletableFuture.completedFuture(entity.teleport(entity));
    }

    @Override
    public CompletionStage<Chunk> getChunkAtAsync(Location location) {
        return CompletableFuture.completedFuture(location.getChunk());
    }

    @Override
    public CompletionStage<Chunk> getChunkAtAsync(World world, int x, int z) {
        return CompletableFuture.completedFuture(world.getChunkAt(x, z));
    }

    @Override
    public BlockState getBlockState(Block block, boolean snapshot) {
        return block.getState();
    }
}
