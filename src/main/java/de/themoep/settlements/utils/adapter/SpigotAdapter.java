package de.themoep.settlements.utils.adapter;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

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
}
