package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAdminJoinCommand extends SubCommand {
    public VillageAdminJoinCommand() {
        super("Forces a player to join a village", "join");
        this.setPermission("village.admin");
        this.setUsage("join <player> <village>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 2) {
            MessageUtils.tell(player, this.getUsage(base), null, null);
            return;
        }
        if (VillageManager.getInstance().getVillageFromName(args[1].toLowerCase()) == null) {
            MessageUtils.tell(player, "&c&lNo &r&6&lvillage&r&c&l with the name &r&6&l{village}&r&c&l exists!", "{village}", args[1]);
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || target.getName() == null || target.getUniqueId() == null) {
            MessageUtils.tell(player, "&c&lNo &r&6&lplayer&r&c&l with the name &r&6&l{player}&r&c&l found!", "{player}", args[0]);
            return;
        }
        Village village = VillageManager.getInstance().getVillageFromName(args[1]);
        Resident res = ResidentManager.getInstance().getResident(target);
        if (res.getVillageName().equals(village.getName())) {
            MessageUtils.tell(player, "&6&l" + target.getName() + "&r&c&l is already in the &r&6&l" + village.getName(), null, null);
            return;
        }
        res.setVillageName(village.getName());
        res.setVillageRank(VillageRank.RESIDENT);
        Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
        MessageUtils.tell(player, "&6&l" + target.getName() + "&r&c&l is now in {townName}", "{townName}", village.getName());
        if (target.isOnline()) {
            MessageUtils.tell((Player) target, MessageUtils.SUCCESS_JOIN_MESSAGE, "{townName}", village.getName());
        }
        MessageUtils.tellResidents(village.getResidents(), MessageUtils.NEW_RESIDENT_MESSAGE, "{player}", target.getName());
    }
}
