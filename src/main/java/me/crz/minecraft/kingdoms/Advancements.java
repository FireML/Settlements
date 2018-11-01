package me.crz.minecraft.kingdoms;

import me.crz.minecraft.kingdoms.essentials.KingdomsEssentialsPlugin;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.logging.Level;

public class Advancements extends Base {
    public Advancements() {
        super();
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        try {
            Player p = e.getPlayer();
            Resident res = ResidentManager.getInstance().getResident(p);
            KingdomsEssentialsPlugin plugin = KingdomsEssentialsPlugin.getInstance();
            if (res != null && res.hasVillage()) {
                if (res.getVillageRank().atLeast(VillageRank.CHIEF)) {
                    plugin.grantAdvancement(p, "root");
                }
            }
        } catch (Exception ex) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error while handling player login event", ex);
        }
    }
}
