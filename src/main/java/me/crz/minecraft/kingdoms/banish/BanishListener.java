package me.crz.minecraft.kingdoms.banish;

import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveBlockEvent;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.TitleBuilder;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BanishListener implements Listener {
    
    @EventHandler
    public void on(PlayerMoveBlockEvent e) {
        VillageManager.getInstance().isResBanished(e.getTo(), e.getPlayer(), (land, p, village) -> {
            sendInfo(p, village, "move");
            e.setCancelled(true);
        });
    }

    @EventHandler
    public void on(PlayerTeleportEvent e) {
        VillageManager.getInstance().isResBanished(e.getTo(), e.getPlayer(), (land, p, village) -> {
            sendInfo(p, village, "teleport");
            e.setCancelled(true);
        });
    }

    private void sendInfo(Player p, Village village, String type) {
        String[] replacements = new String[] {
                "state", village.getState().getTag(),
                "name", village.getName()
        };
        MessageUtils.tell(p, Message.get("banished." + type + ".message"), replacements);
        new TitleBuilder()
                .withTitle(Message.get("banished." + type + ".title").getReplacedText(replacements))
                .withSubtitle(Message.get("banished." + type + ".subtitle").getReplacedText(replacements))
                .withActionbar(Message.get("banished." + type + ".actionbar").getReplacedText(replacements)).send(p);
    }
}
