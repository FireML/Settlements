package me.crz.minecraft.kingdoms.village;

import java.util.ArrayList;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.MaterialUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.crz.minecraft.kingdoms.utils.Vlock;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import me.crz.minecraft.kingdoms.teleporter.TeleportersGUI;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import me.crz.minecraft.kingdoms.rank.VillageRank;
import org.bukkit.ChatColor;

import java.util.Arrays;

import org.bukkit.block.Sign;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.land.LandManager;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VillageListener implements Listener {

    public VillageListener() {
        super();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(AsyncPlayerPreLoginEvent event) {
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
    public void on(PlayerJoinEvent event) {
        Resident res = ResidentManager.getInstance().getResident(event.getPlayer().getUniqueId()); // should be cached
        res.getChannels().removeIf(chatChannel -> !chatChannel.canChat(event.getPlayer()));
        for (ChatChannel channel : ChatChannel.values()) {
            if (channel.canChat(event.getPlayer())) {
                res.getChannels().add(channel);
            }
        }
    }

    @EventHandler
    public void on(SignChangeEvent e) {
        if (e.getLine(0) != null && e.getLine(0).equalsIgnoreCase("[vlock]")) {
            OwnedLand land = LandManager.getInstance().getLand(e.getPlayer().getLocation());
            if (land == null || !land.getOwnerVillage().equals(ResidentManager.getInstance().getResident(e.getPlayer()).getVillage())) {
                MessageUtils.tell(e.getPlayer(), "&c&lYou can only lock things in your own claim!");
                e.getBlock().breakNaturally();
                return;
            }
            e.setLine(0, MessageUtils.replaceColorCodes("&c&l[vlock]"));
            e.setLine(1, MessageUtils.replaceColorCodes("&a&l" + e.getPlayer().getName()));
            MessageUtils.tell(e.getPlayer(), "&7&lThis is now locked to you!");
        }
    }

    @EventHandler
    public void on(BlockBreakEvent e) {
        if (e.getBlock() == null) {
            return;
        }
        if (e.isCancelled()) {
            return;
        }
        OwnedLand land = LandManager.getInstance().getLand(e.getBlock().getLocation());
        if (land == null) {
            return;
        }
        if (e.getBlock().getType() == Material.SIGN || e.getBlock().getType() == Material.WALL_SIGN) {
            Resident res = ResidentManager.getInstance().getResident(e.getPlayer());
            Village village = VillageManager.getInstance().getVillageFromResident(res);
            Sign sign = (Sign) e.getBlock().getState();
            List<String> lines = Arrays.asList(sign.getLines());
            if (ChatColor.stripColor(lines.get(0)).equalsIgnoreCase("[vlock]")) {
                if (village == null) {
                    e.setCancelled(true);
                    return;
                }
                if (!land.getOwnerVillage().equals(res.getVillage())) {
                    MessageUtils.tell(e.getPlayer(), "&c&lYou can't unlock another villages block!");
                    e.setCancelled(true);
                    return;
                }
                if (VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF)) {
                    e.getBlock().breakNaturally();
                } else {
                    if (lines.get(1) != null && ChatColor.stripColor(lines.get(1)).equals(e.getPlayer().getName())) {
                        e.getBlock().breakNaturally();
                        return;
                    }
                    MessageUtils.tell(e.getPlayer(), "&c&lYou require &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + " or higher&r&c&l !");
                    e.setCancelled(true);
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
                if (land.getOwnerVillage().equals(village)) {
                    new TeleportersGUI().build(e.getPlayer());
                } else {
                    MessageUtils.tell(e.getPlayer(), "&c&lYou can't use another villages teleporter");
                }
            }
        }
    }

    @EventHandler
    public void on(PlayerInteractEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getClickedBlock() == null) {
            return;
        }
        OwnedLand land = LandManager.getInstance().getLand(e.getClickedBlock().getLocation());
        if (land == null) {
            return;
        }
        if (e.getClickedBlock().getType() == Material.SIGN || e.getClickedBlock().getType() == Material.WALL_SIGN) {
            Resident res = ResidentManager.getInstance().getResident(e.getPlayer());
            Village village = VillageManager.getInstance().getVillageFromResident(res);
            Sign sign = (Sign) e.getClickedBlock().getState();
            List<String> lines = Arrays.asList(sign.getLines());
            if (ChatColor.stripColor( lines.get(0)).equalsIgnoreCase("[vlock]") && e.getPlayer().isSneaking()) {
                if (land.getOwnerVillage().equals(res.getVillage())) {
                    if (VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF) || ChatColor.stripColor( lines.get(1)).equals(e.getPlayer().getName())) {
                        e.getClickedBlock().breakNaturally();
                    } else {
                        if (e.getPlayer().isOp()) {
                            e.getClickedBlock().breakNaturally();
                            return;
                        }
                        MessageUtils.tell(e.getPlayer(), "&c&lYou require &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + " or higher&r&c&l !");
                    }
                } else {
                    if (e.getPlayer().isOp()) {
                        e.getClickedBlock().breakNaturally();
                        return;
                    }
                    MessageUtils.tell(e.getPlayer(), "&c&lYou can't unlock another villages chests!");
                    return;
                }
            }
        }
        Material mat = e.getClickedBlock().getType();
        if (MaterialUtils.CONTAINERS.contains(mat)) {
            for (Block b : this.getBlocks(e.getClickedBlock().getLocation())) {
                if (this.check(b.getLocation())) {
                    Sign sign2 = (Sign) b.getState();
                    List<String> lines2 = Arrays.asList(sign2.getLines());
                    if (ChatColor.stripColor( lines2.get(0)).equalsIgnoreCase("[vlock]") && !ChatColor.stripColor( lines2.get(1)).equalsIgnoreCase(e.getPlayer().getName())) {
                        if (Vlock.isUUIDPresent(b.getLocation(), e.getPlayer().getUniqueId())) {
                            return;
                        }
                        if (e.getPlayer().isOp()) {
                            return;
                        }
                        MessageUtils.tell(e.getPlayer(), "&c&lThis is currently locked by &r&6&l" + lines2.get(1));
                        e.setCancelled(true);
                        return;
                    }
                }
                if (b.getType() == mat && (mat == Material.CHEST || mat == Material.TRAPPED_CHEST)) {
                    for (Block bl : this.getBlocks(b.getLocation())) {
                        if (!this.check(bl.getLocation())) {
                            continue;
                        }
                        Sign sign3 = (Sign) bl.getState();
                        List<String> lines3 = Arrays.asList(sign3.getLines());
                        if (!ChatColor.stripColor( lines3.get(0)).equalsIgnoreCase("[vlock]") || ChatColor.stripColor( lines3.get(1)).equalsIgnoreCase(e.getPlayer().getName())) {
                            continue;
                        }
                        if (Vlock.isUUIDPresent(bl.getLocation(), e.getPlayer().getUniqueId())) {
                            return;
                        }
                        if (e.getPlayer().isOp()) {
                            return;
                        }
                        MessageUtils.tell(e.getPlayer(), "&c&lThis is currently locked by &r&6&l" + lines3.get(1));
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
        if (mat == Material.HOPPER || mat == Material.FURNACE
                || Tag.WOODEN_DOORS.isTagged(mat) || Tag.BUTTONS.isTagged(mat) || Tag.WOODEN_TRAPDOORS.isTagged(mat)
                || MaterialUtils.FENCE_GATES.contains(mat)) {
            for (Block b : this.getBlocks(e.getClickedBlock().getLocation())) {
                if (!this.check(b.getLocation())) {
                    continue;
                }
                Sign sign2 = (Sign) b.getState();
                List<String> lines2 = Arrays.asList(sign2.getLines());
                if (!ChatColor.stripColor( lines2.get(0)).equalsIgnoreCase("[vlock]") || ChatColor.stripColor( lines2.get(1)).equalsIgnoreCase(e.getPlayer().getName())) {
                    continue;
                }
                if (Vlock.isUUIDPresent(b.getLocation(), e.getPlayer().getUniqueId())) {
                    return;
                }
                if (e.getPlayer().isOp()) {
                    return;
                }
                MessageUtils.tell(e.getPlayer(), "&c&lThis is currently locked by &r&6&l" + lines2.get(1));
                e.setCancelled(true);
            }
        }
    }

    public List<Block> getBlocks(Location location) {
        List<Block> blocks = new ArrayList<>();
        blocks.add(location.getWorld().getBlockAt(location.clone().add(1.0, 0.0, 0.0)));
        blocks.add(location.getWorld().getBlockAt(location.clone().add(-1.0, 0.0, 0.0)));
        blocks.add(location.getWorld().getBlockAt(location.clone().add(0.0, 0.0, 1.0)));
        blocks.add(location.getWorld().getBlockAt(location.clone().add(0.0, 0.0, -1.0)));
        return blocks;
    }

    public boolean check(Location loc) {
        Block b = loc.getWorld().getBlockAt(loc);
        return b != null && (b.getType() == Material.SIGN || b.getType() == Material.WALL_SIGN);
    }
}
