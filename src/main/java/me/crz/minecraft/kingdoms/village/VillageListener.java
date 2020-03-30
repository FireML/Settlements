package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class VillageListener implements Listener {

    public VillageListener() {
        super();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLoginCheck(AsyncPlayerPreLoginEvent event) {
        if (!KingdomsPlugin.getInstance().isRunning() && event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Server not fully started yet!");
        }
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
                if (Utils.shouldResetChannelOnLogin() && res.isListening(ChatChannel.GENERAL)) {
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
    public void onPlayerQuit(PlayerQuitEvent event) {
        ResidentManager.getInstance().uncachePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Resident res = ResidentManager.getInstance().getResident(event.getPlayer()); // should be cached
        boolean activeChanged = false;
        for (ChatChannel channel : ChatChannel.values()) {
            if (channel != ChatChannel.GENERAL) {
                if (channel.autoJoin(event.getPlayer())) {
                    res.getChannels().add(channel);
                } else if (Utils.shouldResetChannelOnLogin()) {
                    res.getChannels().remove(channel);
                    if (res.getActiveChannel() == channel) {
                        activeChanged = res.setActiveChannel(res.getChannels().iterator().next());
                    }
                }
            }
        }
        if (activeChanged) {
            Message.get("command.chat.active").tell(res, "name", res.getActiveChannel().getTag());
        }
        if (res.hasVillage()) {
            if (res.getVillageRank().below(VillageRank.getChief()) && !KingdomsPlugin.getInstance().econ.has(event.getPlayer(), res.getVillage().getTax())) {
                Message.get("tax.debt-action." + res.getVillage().getTaxDebtAction().getKey() + ".cant-pay").tell(event.getPlayer(), "amount", String.valueOf(res.getVillage().getTax()));
            }
            long upkeep = res.getVillage().getUpkeep() - res.getVillage().getTaxIncome();
            if (!res.getVillage().has(upkeep)) {
                Message.get("upkeep.alert.cant-pay").tell(event.getPlayer(), "amount", String.valueOf(upkeep));
            }
            if (res.hasEmpire()) {
                Empire empire = res.getVillage().getEmpire();
                long empireTax = empire.getTax();
                if (!empire.isCapital(res.getVillage()) && !res.getVillage().has(empireTax + upkeep)) {
                    Message.get("empire-tax.debt-action." + empire.getTaxDebtAction().getKey() + ".cant-pay").tell(event.getPlayer(), "amount", String.valueOf(empireTax));
                }
                if (res.getVillageRank().atLeast(VillageRank.getChief())) {
                    long empireUpkeep = empire.getUpkeep() - empire.getTaxIncome();
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPlace(BlockPlaceEvent event) {
        notifyNearbyLandOwners(event.getBlock().getLocation(), event.getPlayer(), "place");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerBreak(BlockBreakEvent event) {
        notifyNearbyLandOwners(event.getBlock().getLocation(), event.getPlayer(), "break");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPlaceHanging(HangingPlaceEvent event) {
        notifyNearbyLandOwners(event.getEntity().getLocation(), event.getPlayer(), "place");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerBreakHanging(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            notifyNearbyLandOwners(event.getEntity().getLocation(), (Player) event.getRemover(), "break");
        }
    }

    private void notifyNearbyLandOwners(Location location, Player player, String type) {
        SimpleChunkLocation chunkLocation = new SimpleChunkLocation(location);
        OwnedLand land = LandManager.getInstance().getLand(chunkLocation);
        if (land == null) {
            Resident resident = ResidentManager.getInstance().getResident(player);
            for (LandOwner landOwner : LandManager.getInstance().getNearbyLandOwners(chunkLocation)) {
                if (!landOwner.equals(resident.getVillage())) {
                    Message.get("close-claims.player." + type).tell(player);
                    break;
                }
            }
        }
    }
}
