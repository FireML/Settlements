package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.village.events.SettlementJoinEvent;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAdminJoinCommand extends SubCommand {
    public VillageAdminJoinCommand(SubCommand parent) {
        super(parent, "join <player> <village>");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 2) {
            return false;
        }
        Village village = VillageManager.getInstance().getVillageFromName(args[1]);
        if (village == null) {
            getError("village-doesnt-exist").tell(sender, "name", args[1]);
            return true;
        }
        Utils.runAsync(() -> {
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target == null) {
                getError("player-not-found").tell(sender, "name", args[0]);
                return;
            }
            if (target.getVillageName().equals(village.getName())) {
                MessageUtils.tell(sender, "&6&l" + target.getName() + "&r&c&l is already in the &r&6&l" + village.getName());
                return;
            }
            Utils.runSync(() -> {
                Utils.callEvent(new SettlementJoinEvent(village, target));
                target.setVillage(village);
                target.setVillageRank(VillageRank.getDefault());
                target.addChannel(ChatChannel.SETTLEMENT);
                Message.get("command.chat.joined").tell(target, "name", ChatChannel.SETTLEMENT.getTag());
                DatabaseManager.getInstance().saveResident(target);
                MessageUtils.tell(sender, "&6&l" + target.getName() + "&r&c&l is now in " + village.getName());
                Player targetP = target.getPlayer();
                if (targetP != null) {
                    MessageUtils.tell(targetP, Message.get("command.village.join.success"), "name", village.getName(), "state", village.getState().getTag());
                }
                MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("command.village.join.new-resident"), "player", target.getName());
            });
        });
        return true;
    }
}
