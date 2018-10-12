package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.Message;
import org.json.JSONObject;
import org.bukkit.block.Block;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Bukkit;

import java.util.UUID;

import me.crz.minecraft.kingdoms.utils.Vlock;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.Material;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageLockCommand extends SubCommand {
    public VillageLockCommand() {
        super("Add, list or remove someone from a lock", "lock");
        this.setPermission("kingdoms.user");
        this.setUsage("lock <add|list|remove> <(optional) player>");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, Message.get("not-in-current"), null, null);
            return;
        }
        if (args.length < 1) {
            MessageUtils.tell(player, "&c" + this.getUsage(base));
            return;
        }
        if (args[0].equalsIgnoreCase("list")) {
            final Block block = player.getTargetBlock(null, 15);
            if (block == null || (block.getType() != Material.SIGN && block.getType() != Material.WALL_SIGN) || ((Sign) block.getState()).getLines().length <= 1 || !ChatColor.stripColor(((Sign) block.getState()).getLines()[0]).equalsIgnoreCase("[vlock]")) {
                MessageUtils.tell(player, "You must be looking at a [vlock] sign to do this!");
                return;
            }
            final StringBuilder sb = new StringBuilder();
            if (Vlock.isLocationVLock(block.getLocation())) {
                final JSONObject obj = Vlock.getVLock(block.getLocation());
                if (obj != null) {
                    String[] split;
                    for (int length = (split = obj.getString("allowed").split(",")).length, i = 0; i < length; ++i) {
                        final String uuid = split[i];
                        try {
                            sb.append("&6");
                            sb.append(Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
                            sb.append("\n");
                        } catch (Exception ex) {
                        }
                    }
                }
            } else {
                sb.append("&c&lNONE");
            }
            MessageUtils.tell(player, "&7Other people with access to this lock: &6\n" + sb.toString());
        }
        if (!args[0].equalsIgnoreCase("remove")) {
            if (args[0].equalsIgnoreCase("add")) {
                if (args.length < 2) {
                    MessageUtils.tell(player, "&c/s lock add &6<player>");
                    return;
                }
                final Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    MessageUtils.tell(player, Message.get("player-not-found"));
                    return;
                }
                final Block block = player.getTargetBlock(null, 15);
                if (block == null || (block.getType() != Material.SIGN && block.getType() != Material.WALL_SIGN)) {
                    MessageUtils.tell(player, "You must be looking at a [vlock] sign to do this!");
                    return;
                }
                final Sign sign = (Sign) block.getState();
                if (sign.getLines().length <= 1 || !ChatColor.stripColor(sign.getLines()[0]).equalsIgnoreCase("[vlock]")) {
                    MessageUtils.tell(player, "You must be looking at a [vlock] sign to do this!");
                    return;
                }
                if (!ChatColor.stripColor(sign.getLines()[1]).equals(player.getName())) {
                    MessageUtils.tell(player, "You must own the lock to do this!");
                    return;
                }
                Vlock.addUUID(block.getLocation(), target.getUniqueId());
                MessageUtils.tell(player, "&7Added player: &6" + target.getDisplayName() + " &7to the lock!");
            }
            return;
        }
        if (args.length < 2) {
            MessageUtils.tell(player, "&c/s lock remove &6<player>");
            return;
        }
        final Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            MessageUtils.tell(player, Message.get("player-not-found"));
            return;
        }
        final Block block = player.getTargetBlock(null, 15);
        if (block == null || (block.getType() != Material.SIGN && block.getType() != Material.WALL_SIGN)) {
            MessageUtils.tell(player, "You must be looking at a [vlock] sign to do this!");
            return;
        }
        final Sign sign = (Sign) block.getState();
        if (sign.getLines().length <= 1 || !ChatColor.stripColor(sign.getLines()[0]).equalsIgnoreCase("[vlock]")) {
            MessageUtils.tell(player, "You must be looking at a [vlock] sign to do this!");
            return;
        }
        if (!ChatColor.stripColor(sign.getLines()[1]).equals(player.getName())) {
            MessageUtils.tell(player, "You must own the lock to do this!");
            return;
        }
        if (!Vlock.isLocationVLock(block.getLocation())) {
            MessageUtils.tell(player, "&cPlayer &6" + target.getName() + " &c isn't added to this lock!");
            return;
        }
        if (Vlock.isUUIDPresent(block.getLocation(), target.getUniqueId())) {
            MessageUtils.tell(player, "&cRemoved player: &6" + target.getDisplayName() + " &cfrom the lock!");
            Vlock.removeUUID(block.getLocation(), target.getUniqueId());
            return;
        }
        MessageUtils.tell(player, "&cPlayer &6" + target.getName() + " &c isn't added to this lock!");
    }
}
