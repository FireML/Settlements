package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.command.CommandSender;

public class VillageAdminEmpireRankCommand extends SubCommand {
    public VillageAdminEmpireRankCommand(SubCommand parent) {
        super(parent, "empirerank <player> <rank>", "setempirerank");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 2) {
            return false;
        }
        EmpireRank rank;
        try {
            rank = EmpireRank.valueOf(args[1]);
        } catch (IllegalArgumentException e) {
            MessageUtils.tell(sender, getError("invalid-rank"), "rank", args[1]);
            return true;
        }
        EmpireRank r = rank;
        Utils.runAsync(() -> {
            Resident tRes = ResidentManager.getInstance().getResident(args[0]);
            if (tRes == null) {
                MessageUtils.tell(sender, getError("player-not-found"), "player", args[0]);
                return;
            }
            if (tRes.hasEmpire()) {
                MessageUtils.tell(sender, getError("not-in-empire"), "player", tRes.getName());
                return;
            }
            Empire empire = tRes.getVillage().getEmpire();
            Resident leader = empire.getCapital().getChief();
            if (r == leader.getEmpireRank()) {
                leader.setEmpireRank(tRes.getEmpireRank());
                tRes.setEmpireRank(r);
                empire.setCapital(tRes.getVillage());

                DatabaseManager.getInstance().saveEmpire(empire);
                DatabaseManager.getInstance().saveResident(leader);
                DatabaseManager.getInstance().saveResident(tRes);
            } else {
                tRes.setEmpireRank(r);
                DatabaseManager.getInstance().saveResident(tRes);
            }
            Message.get("command.empire.setrank.success").tell(sender, "title", "", "setter", sender.getName(), "target", tRes.getName(), "rank", r.getTag());
            Message.get("command.empire.setrank.success").tell(empire.getOnlineResidents(), "title", "", "setter", sender.getName(), "target", tRes.getName(), "rank", r.getTag());
        });
        return true;
    }
}
