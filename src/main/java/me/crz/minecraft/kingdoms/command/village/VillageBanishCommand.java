package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

import java.util.stream.Collectors;

public class VillageBanishCommand extends SubCommand {
    public VillageBanishCommand() {
        super("Banishes a player from your village", "banish");
        this.setPermission("village.user");
        this.setUsage("banish <list|add|remove> <name>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        if (!village.getChief().equals(player.getUniqueId())) {
            MessageUtils.tell(player, MessageUtils.NO_PERMISSION_MESSAGE, null, null);
            return;
        }
        if (args.length < 1) {
            MessageUtils.tell(player, "&c" + this.getUsage(base));
            return;
        }
        if (args[0].equalsIgnoreCase("list")) {
            Utils.runAsync(() -> MessageUtils.tell(player, "&7------- &cBanished&7 -------\n&c"
                    + village.getBanishedResidents().stream()
                    .map(id -> {
                        Resident res = ResidentManager.getInstance().getResident(id);
                        if (res != null) {
                            return res.getName();
                        }
                        return id.toString();
                    }).collect(Collectors.joining("&7,&c"))));
        } else if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 2) {
                MessageUtils.tell(player, "&c" + this.getUsage(base));
                return;
            }
            Utils.runAsync(() -> {
                Resident res = ResidentManager.getInstance().getResident(args[1]);
                if (res == null) {
                    MessageUtils.tell(player, MessageUtils.PLAYER_NOT_FOUND_MESSAGE);
                    return;
                }
                if (res.getVillageName().equals(village.getName())) {
                    VillageManager.getInstance().leaveVillage(res);
                }
                village.addBanishedResident(res);
                DatabaseManager.getInstance().saveVillage(village);
                MessageUtils.tellResidents(village.getOnlineResidents(), "&7Player &6" + res.getName() + "&7 has been &cbanished &7from your settlement!");
            });
        } else if (!args[0].equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                MessageUtils.tell(player, "&c" + this.getUsage(base));
                return;
            }
            Utils.runAsync(() -> {
                Resident res = ResidentManager.getInstance().getResident(args[1]);
                if (res == null) {
                    MessageUtils.tell(player, MessageUtils.PLAYER_NOT_FOUND_MESSAGE);
                    return;
                }
                village.removeBanishedResident(res);
                Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
                MessageUtils.tellResidents(village.getOnlineResidents(), "&7Player &6" + res.getName() + "&7 has been &aun-banished &7from your settlement!");
            });
        } else {
            MessageUtils.tell(player, "&c" + this.getUsage(base));
        }
    }
}
