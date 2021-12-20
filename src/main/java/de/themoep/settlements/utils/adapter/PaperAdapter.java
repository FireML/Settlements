package de.themoep.settlements.utils.adapter;

import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.text.PaperComponents;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.BaseComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.chat.TextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PaperAdapter implements ServerAdapter {

    @Override
    public void setProfile(SkullMeta skull, UUID uuid, String name, String... properties) {
        PlayerProfile profile = Bukkit.createProfile(uuid, name);
        for (int i = 0; i + 1 < properties.length; i += 2) {
            profile.setProperty(new ProfileProperty(properties[i], properties[i+1]));
        }
        skull.setPlayerProfile(profile);
    }

    @Override
    public void sendTitle(Player player, BaseComponent[] title, BaseComponent[] subTitle, int in, int stay, int out) {
        player.sendTitle(new Title(title, subTitle, in, stay, out));
    }

    @Override
    public InventoryHolder getHolder(Inventory inventory) {
        return inventory.getHolder(false);
    }

    @Override
    public void markOwned(Item item, Player player) {
        item.setPickupDelay(0);
        item.setOwner(player.getUniqueId());
        item.setCanMobPickup(false);
    }

    @Override
    public CommandMap getCommandMap() {
        return Bukkit.getServer().getCommandMap();
    }

    @Override
    public ItemMeta createBookMeta(BookMeta bookMeta, String title, String author, List<BaseComponent[]> pages) {
        bookMeta.setTitle(title);
        bookMeta.setAuthor(author);

        return (ItemMeta) bookMeta.pages(pages.stream()
                .map(c -> GsonComponentSerializer.builder().build().deserialize(ComponentSerializer.toString(c)))
                .collect(Collectors.toList()));
    }
}
