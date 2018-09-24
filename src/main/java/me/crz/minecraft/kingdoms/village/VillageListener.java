package me.crz.minecraft.kingdoms.village;

import java.util.ArrayList;
import org.bukkit.Location;
import java.util.Iterator;
import me.crz.minecraft.kingdoms.utils.Vlock;
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

public class VillageListener implements Listener
{
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
        if (e.getBlock().getType().equals((Object)Material.SIGN) || e.getBlock().getType().equals((Object)Material.WALL_SIGN) || e.getBlock().getType().equals((Object)Material.SIGN_POST)) {
            final Resident res = ResidentManager.getInstance().getResident(e.getPlayer());
            final Village village = VillageManager.getInstance().getVillageFromResident(res);
            final Sign sign = (Sign)e.getBlock().getState();
            final List<String> lines = Arrays.<String>asList(sign.getLines());
            if (ChatColor.stripColor((String)lines.get(0)).equalsIgnoreCase("[vlock]")) {
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
                }
                else {
                    if (lines.get(1) != null && ChatColor.stripColor((String)lines.get(1)).equals(e.getPlayer().getName())) {
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
        if (e.getClickedBlock().getType().equals((Object)Material.SIGN) || e.getClickedBlock().getType().equals((Object)Material.WALL_SIGN) || e.getClickedBlock().getType().equals((Object)Material.SIGN_POST)) {
            final Resident res = ResidentManager.getInstance().getResident(e.getPlayer());
            final Village village = VillageManager.getInstance().getVillageFromResident(res);
            if (village == null) {
                return;
            }
            final Sign sign = (Sign)e.getClickedBlock().getState();
            final List<String> lines = Arrays.<String>asList(sign.getLines());
            if (lines.size() < 1) {
                return;
            }
            if (ChatColor.stripColor((String)lines.get(0)).equalsIgnoreCase("[teleporter]")) {
                if (land.getOwnerVillage().equals(village.getName())) {
                    new TeleportersGUI().build(e.getPlayer());
                }
                else {
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
        if (e.getClickedBlock().getType().equals((Object)Material.SIGN) || e.getClickedBlock().getType().equals((Object)Material.WALL_SIGN) || e.getClickedBlock().getType().equals((Object)Material.SIGN_POST)) {
            final Resident res = ResidentManager.getInstance().getResident(e.getPlayer());
            final Village village = VillageManager.getInstance().getVillageFromResident(res);
            final Sign sign = (Sign)e.getClickedBlock().getState();
            final List<String> lines = Arrays.<String>asList(sign.getLines());
            if (ChatColor.stripColor((String)lines.get(0)).equalsIgnoreCase("[vlock]") && e.getPlayer().isSneaking()) {
                if (land.getOwnerVillage().equals(res.getVillageName())) {
                    if (VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF) || ChatColor.stripColor((String)lines.get(1)).equals(e.getPlayer().getName())) {
                        e.getClickedBlock().breakNaturally();
                    }
                    else {
                        if (e.getPlayer().isOp()) {
                            e.getClickedBlock().breakNaturally();
                            return;
                        }
                        MessageUtils.tell(e.getPlayer(), "&c&lYou require &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + " or higher&r&c&l !");
                    }
                }
                else {
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
        if (Arrays.<Material>asList(Material.TRAPPED_CHEST, Material.CHEST, Material.FURNACE).contains(mat) || mat.name().contains("SHULKER_BOX")) {
            for (final Block b : this.getBlocks(e.getClickedBlock().getLocation())) {
                if (this.check(b.getLocation())) {
                    final Sign sign2 = (Sign)b.getState();
                    final List<String> lines2 = Arrays.<String>asList(sign2.getLines());
                    if (ChatColor.stripColor((String)lines2.get(0)).equalsIgnoreCase("[vlock]") && !ChatColor.stripColor((String)lines2.get(1)).equalsIgnoreCase(e.getPlayer().getName())) {
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
                if (!mat.equals((Object)Material.FURNACE) && b != null && b.getType().equals((Object)mat)) {
                    for (final Block bl : this.getBlocks(b.getLocation())) {
                        if (!this.check(bl.getLocation())) {
                            continue;
                        }
                        final Sign sign3 = (Sign)bl.getState();
                        final List<String> lines3 = Arrays.<String>asList(sign3.getLines());
                        if (!ChatColor.stripColor((String)lines3.get(0)).equalsIgnoreCase("[vlock]") || ChatColor.stripColor((String)lines3.get(1)).equalsIgnoreCase(e.getPlayer().getName())) {
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
        if (Arrays.<Material>asList(Material.ACACIA_DOOR, Material.BIRCH_DOOR, Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR, Material.SPRUCE_DOOR, Material.TRAP_DOOR, Material.WOOD_DOOR, Material.STONE_BUTTON, Material.WOOD_BUTTON, Material.HOPPER, Material.FURNACE, Material.FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.SPRUCE_FENCE_GATE).contains(mat)) {
            for (final Block b : this.getBlocks(e.getClickedBlock().getLocation())) {
                if (!this.check(b.getLocation())) {
                    continue;
                }
                final Sign sign2 = (Sign)b.getState();
                final List<String> lines2 = Arrays.<String>asList(sign2.getLines());
                if (!ChatColor.stripColor((String)lines2.get(0)).equalsIgnoreCase("[vlock]") || ChatColor.stripColor((String)lines2.get(1)).equalsIgnoreCase(e.getPlayer().getName())) {
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
        final List<Block> blocks = new ArrayList<Block>();
        blocks.add(location.getWorld().getBlockAt(location.clone().add(1.0, 0.0, 0.0)));
        blocks.add(location.getWorld().getBlockAt(location.clone().add(-1.0, 0.0, 0.0)));
        blocks.add(location.getWorld().getBlockAt(location.clone().add(0.0, 0.0, 1.0)));
        blocks.add(location.getWorld().getBlockAt(location.clone().add(0.0, 0.0, -1.0)));
        return blocks;
    }
    
    public boolean check(final Location loc) {
        final Block b = loc.getWorld().getBlockAt(loc);
        return b != null && (b.getType().equals((Object)Material.SIGN) || b.getType().equals((Object)Material.WALL_SIGN) || b.getType().equals((Object)Material.SIGN_POST));
    }
}
