package me.crz.minecraft.kingdoms.village;

import java.util.ArrayList;

import me.crz.minecraft.kingdoms.utils.MaterialUtils;
import org.bukkit.Location;

import me.crz.minecraft.kingdoms.utils.Vlock;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import me.crz.minecraft.kingdoms.teleporter.TeleportersGUI;
import org.bukkit.event.player.PlayerInteractEvent;

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
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Listener;

public class VillageListener implements Listener {

    public VillageListener() {
        super();
    }

    @EventHandler
    public void on(final PlayerJoinEvent e) {
        final Resident res = ResidentManager.getInstance().getResident(e.getPlayer());
        res.setActiveChannel(ChatChannel.GENERAL);
        ResidentManager.getInstance().saveResident(res);
    }

    @EventHandler
    public void on(final SignChangeEvent e) {
        if (e.getLine(0) != null && e.getLine(0).equalsIgnoreCase("[vlock]")) {
            final OwnedLand land = LandManager.getInstance().getLandFromLocation(e.getPlayer().getLocation());
            if (land == null || !land.getOwnerVillage().equals(ResidentManager.getInstance().getResident(e.getPlayer()).getVillageName())) {
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
    public void on(final BlockBreakEvent e) {
        if (e.getBlock() == null) {
            return;
        }
        if (e.isCancelled()) {
            return;
        }
        final OwnedLand land = LandManager.getInstance().getLandFromLocation(e.getBlock().getLocation());
        if (land == null) {
            return;
        }
        if (e.getBlock().getType() == Material.SIGN || e.getBlock().getType() == Material.WALL_SIGN) {
            final Resident res = ResidentManager.getInstance().getResident(e.getPlayer());
            final Village village = VillageManager.getInstance().getVillageFromResident(res);
            final Sign sign = (Sign) e.getBlock().getState();
            final List<String> lines = Arrays.asList(sign.getLines());
            if (ChatColor.stripColor(lines.get(0)).equalsIgnoreCase("[vlock]")) {
                if (village == null) {
                    e.setCancelled(true);
                    return;
                }
                if (!land.getOwnerVillage().equals(res.getVillageName())) {
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
    public void onSign(final PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) {
            return;
        }
        if (e.isCancelled()) {
            return;
        }
        final OwnedLand land = LandManager.getInstance().getLandFromLocation(e.getClickedBlock().getLocation());
        if (land == null) {
            return;
        }
        if (e.getClickedBlock().getType() == Material.SIGN || e.getClickedBlock().getType() == Material.WALL_SIGN) {
            final Resident res = ResidentManager.getInstance().getResident(e.getPlayer());
            final Village village = VillageManager.getInstance().getVillageFromResident(res);
            if (village == null) {
                return;
            }
            final Sign sign = (Sign) e.getClickedBlock().getState();
            final List<String> lines = Arrays.asList(sign.getLines());
            if (lines.size() < 1) {
                return;
            }
            if (ChatColor.stripColor( lines.get(0)).equalsIgnoreCase("[teleporter]")) {
                if (land.getOwnerVillage().equals(village.getName())) {
                    new TeleportersGUI().build(e.getPlayer());
                } else {
                    MessageUtils.tell(e.getPlayer(), "&c&lYou can't use another villages teleporter");
                }
            }
        }
    }

    @EventHandler
    public void on(final PlayerInteractEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getClickedBlock() == null) {
            return;
        }
        final OwnedLand land = LandManager.getInstance().getLandFromLocation(e.getClickedBlock().getLocation());
        if (land == null) {
            return;
        }
        if (e.getClickedBlock().getType() == Material.SIGN || e.getClickedBlock().getType() == Material.WALL_SIGN) {
            final Resident res = ResidentManager.getInstance().getResident(e.getPlayer());
            final Village village = VillageManager.getInstance().getVillageFromResident(res);
            final Sign sign = (Sign) e.getClickedBlock().getState();
            final List<String> lines = Arrays.asList(sign.getLines());
            if (ChatColor.stripColor( lines.get(0)).equalsIgnoreCase("[vlock]") && e.getPlayer().isSneaking()) {
                if (land.getOwnerVillage().equals(res.getVillageName())) {
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
        final Material mat = e.getClickedBlock().getType();
        if (MaterialUtils.CONTAINERS.contains(mat)) {
            for (final Block b : this.getBlocks(e.getClickedBlock().getLocation())) {
                if (this.check(b.getLocation())) {
                    final Sign sign2 = (Sign) b.getState();
                    final List<String> lines2 = Arrays.asList(sign2.getLines());
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
                    for (final Block bl : this.getBlocks(b.getLocation())) {
                        if (!this.check(bl.getLocation())) {
                            continue;
                        }
                        final Sign sign3 = (Sign) bl.getState();
                        final List<String> lines3 = Arrays.asList(sign3.getLines());
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
            for (final Block b : this.getBlocks(e.getClickedBlock().getLocation())) {
                if (!this.check(b.getLocation())) {
                    continue;
                }
                final Sign sign2 = (Sign) b.getState();
                final List<String> lines2 = Arrays.asList(sign2.getLines());
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

    public List<Block> getBlocks(final Location location) {
        final List<Block> blocks = new ArrayList<>();
        blocks.add(location.getWorld().getBlockAt(location.clone().add(1.0, 0.0, 0.0)));
        blocks.add(location.getWorld().getBlockAt(location.clone().add(-1.0, 0.0, 0.0)));
        blocks.add(location.getWorld().getBlockAt(location.clone().add(0.0, 0.0, 1.0)));
        blocks.add(location.getWorld().getBlockAt(location.clone().add(0.0, 0.0, -1.0)));
        return blocks;
    }

    public boolean check(final Location loc) {
        final Block b = loc.getWorld().getBlockAt(loc);
        return b != null && (b.getType() == Material.SIGN || b.getType() == Material.WALL_SIGN);
    }
}
