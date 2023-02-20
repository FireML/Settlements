package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;

public class SettlementSetRankCommand extends SettlementSubCommand {
    public SettlementSetRankCommand(SubCommand parent) {
        super(parent, "setrank <player> <rank>", "rank");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Settlement settlement = res.getSettlement();
        if (args.length <= 1) {
            getMessage("rank-usage.head").tell(res, settlement.getReplacements(this,
                    "name", name(),
                    "command", "/" + base + " " + alias,
                    "usage", getUsage(base),
                    "description", getDescription()
            ));
            for (SettlementRank rank : SettlementRank.values()) {
                if (rank.below(res.getSettlementRank())) {
                    getMessage("rank-usage.entry").tell(res,
                            "usage", getUsage(base).replace("<rank>", rank.getTag(res.getSettlement())),
                            "rank", rank.getTag(res.getSettlement()),
                            "description", getText("rank." + rank.getKey())
                    );
                }
            }
            if (getMessage("rank-usage.footer").exists()) {
                getMessage("rank-usage.footer").tell(res, settlement.getReplacements(this,
                        "name", name(),
                        "command", "/" + base + " " + alias,
                        "usage", getUsage(base),
                        "description", getDescription()
                ));
            }
            return true;
        }

        Resident tres = settlement.getResident(args[0]);
        if (tres == null) {
            getError("not-in-current").tell(res, "state", settlement.getState().getTag());
            return true;
        }

        SettlementRank rank = SettlementRank.get(args[1]);

        if (rank == null) {
            getError("invalid-rank").tell(res, "rank", args[1]);
        } else if (rank == SettlementRank.getChief()) {
            getError("cant-set-chief").tell(res, "base", base, "state", settlement.getState().getTag());
        } else if (!res.getSettlementRank().above(tres.getSettlementRank())) {
            getMessage("cant-set-higher").tell(res, "rank", res.getSettlementRank().getTag(settlement));
        } else  if (!res.getSettlementRank().above(rank)) {
            getError("rank-required").tell(res, "rank", SettlementRank.values().length > rank.ordinal() + 1 ? SettlementRank.values()[rank.ordinal() + 1].getTag() : "-");
        } else {
                tres.setSettlementRank(rank);
                DatabaseManager.getInstance().saveResident(tres);
                getMessage("success").tell(settlement.getOnlineResidents(),
                        "rank", rank.getTag(settlement),
                        "setter", res.getName(),
                        "title", res.getSettlementTitle(),
                        "displaytitle", res.getDisplayTitle(),
                        "target", tres.getName());
        }
        return true;
    }
}
