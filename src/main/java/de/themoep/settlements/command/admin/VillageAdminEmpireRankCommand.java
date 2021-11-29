package de.themoep.settlements.command.admin;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
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
            Empire empire = tRes.getSettlement().getEmpire();
            if (r == EmpireRank.getLeader()) {
                Resident leader = empire.getCapital().getChief();
                leader.setEmpireRank(EmpireRank.getLeader().next());
                tRes.setEmpireRank(r);
                empire.setCapital(tRes.getSettlement());

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
