package me.crz.minecraft.kingdoms.banish;

import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveChunkEvent;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.TitleBuilder;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BanishListener implements Listener, ConfigMessages {
    
    @EventHandler
    public void on(PlayerMoveChunkEvent e) {
        VillageManager.getInstance().isResBanished(LandManager.getInstance().getLand(e.getTo()), e.getPlayer(), (land, p, village) -> {
            sendInfo(p, village, "move");
            e.setCancelled(true);
        });
    }

    @EventHandler
    public void on(PlayerTeleportEvent e) {
        VillageManager.getInstance().isResBanished(LandManager.getInstance().getLand(e.getTo()), e.getPlayer(), (land, p, village) -> {
            sendInfo(p, village, "teleport");
            e.setCancelled(true);
        });
    }

    private void sendInfo(Player p, Village village, String type) {
        String[] replacements = new String[] {
                "state", village.getState().getTag(),
                "name", village.getName()
        };
        getMessage(type + ".message").tell(p, replacements);
        new TitleBuilder()
                .withTitle(getMessage(type + ".title").getReplacedText(replacements))
                .withSubtitle(getMessage(type + ".subtitle").getReplacedText(replacements))
                .withActionbar(getMessage(type + ".actionbar").getReplacedText(replacements))
                .send(p);
    }

    @Override
    public String type() {
        return "";
    }

    @Override
    public String name() {
        return "banished";
    }
}
