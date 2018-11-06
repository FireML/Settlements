package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.teleporter.TeleportersGUI;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.Arrays;
import java.util.List;

public class VillageListener implements Listener {

    public VillageListener() {
        super();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            // try to load the data and if it doesn't exist create it
            Resident res = ResidentManager.getInstance().getResident(event.getUniqueId());
            if (res == null) {
                res = new Resident(event.getUniqueId(), event.getName());
                res.updateLastLogin();
                ResidentManager.getInstance().addResident(res);
            } else {
                res.updateLastLogin();
                if (!res.getName().equalsIgnoreCase(event.getName())) {
                    ResidentManager.getInstance().invalidateName(res.getName());
                    res.setName(event.getName());
                    ResidentManager.getInstance().addResident(res);
                }
            }
            Resident finalRes = res;
            Bukkit.getScheduler().runTaskAsynchronously(KingdomsPlugin.getInstance(),
                    () -> DatabaseManager.getInstance().saveResident(finalRes));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Resident res = ResidentManager.getInstance().getResident(event.getPlayer().getUniqueId()); // should be cached
        res.getChannels().removeIf(chatChannel -> !chatChannel.canChat(event.getPlayer()));
        for (ChatChannel channel : ChatChannel.values()) {
            if (channel.canChat(event.getPlayer())) {
                res.getChannels().add(channel);
            }
        }
        if (res.hasVillage()) {
            if (res.getVillageRank().below(VillageRank.CHIEF) && !KingdomsPlugin.getInstance().econ.has(event.getPlayer(), res.getVillage().getTax())) {
                MessageUtils.tell(event.getPlayer(), Message.get("tax.cant-pay"), "amount", String.valueOf(res.getVillage().getTax()));
            }
            long upkeep = res.getVillage().getUpkeep();
            if (!res.getVillage().has(upkeep - ((res.getVillage().getResidentIds().size() - 1) * res.getVillage().getTax()))) {
                MessageUtils.tell(event.getPlayer(), Message.get("upkeep.alert.cant-pay"), "amount", String.valueOf(upkeep));
            }
            if (res.hasEmpire()) {
                long empireTax = res.getVillage().getEmpire().getTax();
                if (!res.getVillage().has(empireTax - upkeep)) {
                    MessageUtils.tell(event.getPlayer(), Message.get("empire-tax.cant-pay"), "amount", String.valueOf(empireTax));
                }
            }
        }
    }

    @EventHandler
    public void onSign(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) {
            return;
        }
        if (e.isCancelled()) {
            return;
        }
        OwnedLand land = LandManager.getInstance().getLand(e.getClickedBlock().getLocation());
        if (land == null) {
            return;
        }
        if (e.getClickedBlock().getType() == Material.SIGN || e.getClickedBlock().getType() == Material.WALL_SIGN) {
            Resident res = ResidentManager.getInstance().getResident(e.getPlayer());
            Village village = VillageManager.getInstance().getVillageFromResident(res);
            if (village == null) {
                return;
            }
            Sign sign = (Sign) e.getClickedBlock().getState();
            List<String> lines = Arrays.asList(sign.getLines());
            if (lines.size() < 1) {
                return;
            }
            if (ChatColor.stripColor( lines.get(0)).equalsIgnoreCase("[teleporter]")) {
                if (land.getLandOwner().equals(village)) {
                    new TeleportersGUI().build(e.getPlayer());
                } else {
                    MessageUtils.tell(e.getPlayer(), "&c&lYou can't use another villages teleporter");
                }
            }
        }
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            ResidentManager.getInstance().getResident(event.getPlayer()).setResourcePack(true);
        }
    }
}
