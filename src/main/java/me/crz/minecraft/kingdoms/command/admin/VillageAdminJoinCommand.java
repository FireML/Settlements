package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAdminJoinCommand extends SubCommand {
    public VillageAdminJoinCommand(SubCommand parent) {
        super(parent, "join <player> <village>");
        setPermission("kingdoms.admin");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (args.length < 2) {
            return false;
        }
        if (VillageManager.getInstance().getVillageFromName(args[1].toLowerCase()) == null) {
            MessageUtils.tell(player, "&c&lNo &r&6&lvillage&r&c&l with the name &r&6&l{village}&r&c&l exists!", "{village}", args[1]);
            return true;
        }
        Utils.runAsync(() -> {
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target == null) {
                MessageUtils.tell(player, "&c&lNo &r&6&lplayer&r&c&l with the name &r&6&l{player}&r&c&l found!", "{player}", args[0]);
                return;
            }
            Village village = VillageManager.getInstance().getVillageFromName(args[1]);
            if (target.getVillageName().equals(village.getName())) {
                MessageUtils.tell(player, "&6&l" + target.getName() + "&r&c&l is already in the &r&6&l" + village.getName());
                return;
            }
            target.setVillage(village);
            target.setVillageRank(VillageRank.RESIDENT);
            DatabaseManager.getInstance().saveResident(target);
            MessageUtils.tell(player, "&6&l" + target.getName() + "&r&c&l is now in {townName}", "{townName}", village.getName());
            Player targetP = target.getPlayer();
            if (targetP != null) {
                MessageUtils.tell(targetP, Message.get("command.village.join.success"), "name", village.getName(), "state", village.getState().getTag());
            }
            MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("command.village.join.new-resident"), "player", target.getName());
        });
        return true;
    }
}
