package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.rank.EmpireRank;

public class EmpireSetRankCommand extends EmpireSubCommand {
    public EmpireSetRankCommand(SubCommand parent) {
        super(parent, "setrank <player> <rank>", "rank");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Empire empire = res.getSettlement().getEmpire();
        if (args.length <= 1) {
            getMessage("rank-usage.head").tell(res, empire.getReplacements(this,
                    "name", name(),
                    "command", "/" + base + " " + alias,
                    "usage", getUsage(base),
                    "description", getDescription()
            ));
            for (EmpireRank rank : EmpireRank.values()) {
                if (rank.below(res.getEmpireRank())) {
                    getMessage("rank-usage.entry").tell(res,
                            "usage", getUsage(base).replace("<rank>", rank.getTag()),
                            "rank", rank.getTag(),
                            "description", getText("rank." + rank.getKey())
                    );
                }
            }
            if (getMessage("rank-usage.footer").exists()) {
                getMessage("rank-usage.footer").tell(res, empire.getReplacements(this,
                        "name", name(),
                        "command", "/" + base + " " + alias,
                        "usage", getUsage(base),
                        "description", getDescription()
                ));
            }
            return true;
        }

        Resident tres = empire.getResident(args[0]);
        if (tres == null) {
            getError("not-in-empire").tell(res);
            return true;
        }

        EmpireRank rank = EmpireRank.get(args[1]);

        if (rank == null) {
            getError("invalid-rank").tell(res, "rank", args[1]);
        } else if (rank.equals(EmpireRank.getLeader())) {
            getError("cant-set-leader").tell(res, "base", base);
        } else if (!res.getEmpireRank().above(tres.getEmpireRank())) {
            getMessage("cant-set-higher").tell(res, "rank", res.getEmpireRank().getTag());
        } else  if (!res.getEmpireRank().above(rank)) {
            getError("rank-required").tell(res, "rank", EmpireRank.values().length > rank.ordinal() + 1 ? EmpireRank.values()[rank.ordinal() + 1].getTag() : "-");
        } else {
            tres.setEmpireRank(rank);
            DatabaseManager.getInstance().saveResident(tres);
            getMessage("success").tell(empire.getOnlineResidents(),
                    "rank", rank.getTag(),
                    "title", res.getSettlementTitle(),
                    "displaytitle", res.getDisplayTitle(),
                    "setter", res.getName(),
                    "target", tres.getName());
        }
        return true;
    }
}
