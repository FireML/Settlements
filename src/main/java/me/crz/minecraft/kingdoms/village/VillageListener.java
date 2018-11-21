package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.teleporter.TeleportersGUI;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
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
                if (Utils.shouldResetChannelOnLogin()) {
                    res.setActiveChannel(ChatChannel.GENERAL);
                    Message.get("command.chat.joined").tell(res, "name", ChatChannel.GENERAL.getTag());
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
        for (ChatChannel channel : ChatChannel.values()) {
            if (channel.canChat(event.getPlayer())) {
                res.getChannels().add(channel);
            } else {
                res.getChannels().remove(channel);
            }
        }
        if (res.hasVillage()) {
            if (res.getVillageRank().below(VillageRank.CHIEF) && !KingdomsPlugin.getInstance().econ.has(event.getPlayer(), res.getVillage().getTax())) {
                Message.get("tax.cant-pay").tell(event.getPlayer(), "amount", String.valueOf(res.getVillage().getTax()));
            }
            long upkeep = res.getVillage().getUpkeep() - (res.getVillage().getResidentIds().size() - 1) * res.getVillage().getTax();
            if (!res.getVillage().has(upkeep)) {
                Message.get("upkeep.alert.cant-pay").tell(event.getPlayer(), "amount", String.valueOf(upkeep));
            }
            if (res.hasEmpire()) {
                Empire empire = res.getVillage().getEmpire();
                long empireTax = empire.getTax();
                if (!empire.isCapital(res.getVillage()) && !empire.has(empireTax - upkeep)) {
                    Message.get("empire-tax.cant-pay").tell(event.getPlayer(), "amount", String.valueOf(empireTax));
                }
                if (res.getVillageRank().atLeast(VillageRank.CHIEF)) {
                    long empireUpkeep = empire.getUpkeep() - (empire.getMemberIds().size() - 1) * empire.getTax();
                    if (!empire.has(empireUpkeep)) {
                        Message.get("upkeep.empire-alert.cant-pay").tell(event.getPlayer(), "amount", String.valueOf(empireUpkeep));
                    }
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
