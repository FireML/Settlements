package me.crz.minecraft.kingdoms.banish;

import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveBlockEvent;
import me.crz.minecraft.kingdoms.Base;

public class BanishListener extends Base {
    public BanishListener() {
        super();
    }

    @EventHandler
    public void on(final PlayerMoveBlockEvent e) {
        VillageManager.getInstance().isResBanished(e.getTo(), e.getPlayer(), (land, p, village) -> {
            MessageUtils.tell(p, "&c&lYou are banished from &6&l" + village.getName());
            e.setCancelled(true);
        });
    }

    @EventHandler
    public void on(final PlayerTeleportEvent e) {
        VillageManager.getInstance().isResBanished(e.getTo(), e.getPlayer(), (land, p, village) -> {
            MessageUtils.tell(p, "&c&lYou cannot teleport to &6&l" + village.getName() + "&c&l because you are banished");
            e.setCancelled(true);
        });
    }
}
