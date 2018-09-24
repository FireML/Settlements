package me.crz.minecraft.kingdoms;

import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.advancementapi.AdvancementAPI;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.essentials.KingdomsEssentialsPlugin;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.event.player.PlayerJoinEvent;

public class Advancements extends Base
{
    public Advancements() {
        super();
    }
    
    @EventHandler
    public void on(final PlayerJoinEvent e) {
        try {
            final Player p = e.getPlayer();
            final Resident res = ResidentManager.getInstance().getResident(p);
            final KingdomsEssentialsPlugin plugin = KingdomsEssentialsPlugin.getInstance();
            if (ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                final Village v = VillageManager.getInstance().getVillageFromResident(res);
                if (!p.getAdvancementProgress(((AdvancementAPI)plugin.advancements.get("root")).getAdvancement()).isDone() && v.getChief().equals(res.getUUID())) {
                    ((AdvancementAPI)plugin.advancements.get("root")).grant(new Player[] { p });
                }
            }
        }
        catch (Exception ex) {}
    }
}
