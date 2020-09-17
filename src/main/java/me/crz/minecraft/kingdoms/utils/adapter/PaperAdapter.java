package me.crz.minecraft.kingdoms.utils.adapter;

import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

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
}
