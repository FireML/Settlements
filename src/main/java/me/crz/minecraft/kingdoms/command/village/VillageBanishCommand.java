package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.OfflinePlayer;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MojangAPI;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

import java.util.UUID;

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
            StringBuilder sb = new StringBuilder("&7------- &cBanished&7 -------\n&c");
            for (UUID id : village.getBanishedResidents()) {
                sb.append(String.valueOf(MojangAPI.getInstance().getName(id))).append("&7,&c");
            }
            MessageUtils.tell(player, sb.toString());
            return;
        }
        if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 2) {
                MessageUtils.tell(player, "&c" + this.getUsage(base));
                return;
            }
            Resident res = ResidentManager.getInstance().getResident(args[1]);
            if (res == null) {
                MessageUtils.tell(player, MessageUtils.PLAYER_NOT_FOUND_MESSAGE);
                return;
            }
            if (res.getVillageName().equals(village.getName())) {
                VillageManager.getInstance().leaveVillage(res);
            }
            village.addBanishedResident(res);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
            MessageUtils.tellResidents(village.getOnlineResidents(), "&7Player &6" + args[1] + "&7 has been &cbanished &7from your settlement!");

        } else {
            if (!args[0].equalsIgnoreCase("remove")) {
                MessageUtils.tell(player, "&c" + this.getUsage(base));
                return;
            }
            if (args.length < 2) {
                MessageUtils.tell(player, "&c" + this.getUsage(base));
                return;
            }
            Resident res = ResidentManager.getInstance().getResident(args[1]);
            if (res == null) {
                MessageUtils.tell(player, MessageUtils.PLAYER_NOT_FOUND_MESSAGE);
                return;
            }
            village.removeBanishedResident(res);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
            MessageUtils.tellResidents(village.getOnlineResidents(), "&7Player &6" + args[1] + "&7 has been &aun-banished &7from your settlement!");
        }
    }
}
