package me.crz.minecraft.kingdoms.customevents.listeners;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import me.crz.minecraft.kingdoms.customevents.CustomEventManager;
import me.crz.minecraft.kingdoms.customevents.EntityPrimeTntEvent;
import me.crz.minecraft.kingdoms.customevents.HostileCreaturePreSpawnEvent;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.portal.Portal;
import me.crz.minecraft.kingdoms.portal.PortalManager;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PaperListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPhantomPreSpawn(PhantomPreSpawnEvent e) {
        if (!new HostileCreaturePreSpawnEvent(e.getType(), e.getSpawnLocation(), e.getReason()).callEvent()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHostilePreSpawn(PreCreatureSpawnEvent e) {
        if ((e.getType().getEntityClass() != null && Monster.class.isAssignableFrom(e.getType().getEntityClass()))
                || CustomEventManager.SPECIAL_HOSTILE.contains(e.getType())) {
            if (!new HostileCreaturePreSpawnEvent(e.getType(), e.getSpawnLocation(), e.getReason()).callEvent()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTntPrime(TNTPrimeEvent e) {
        if (!new EntityPrimeTntEvent(e.getBlock(), EntityPrimeTntEvent.Reason.valueOf(e.getReason().name()), e.getPrimerEntity()).callEvent()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPortalDestroy(BlockDestroyEvent e) {
        if (e.getBlock().getType() != Material.NETHER_PORTAL && e.getBlock().getType() != Material.OBSIDIAN) {
            return;
        }

        OwnedLand land = LandManager.getInstance().getLand(e.getBlock().getLocation());
        if (land == null || !(land.getLandOwner() instanceof Village)) {
            return;
        }

        Portal portal = PortalManager.getInstance().getPortal(e.getBlock());

        if (portal != null && PortalManager.getInstance().removePortal(portal)) {
            Message.get("portal.destroy-broadcast").tell(land.getLandOwner().getOnlineResidents(), portal.getReplacements());
        }
    }
}
