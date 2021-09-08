package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.rank.VillageRank;

public class VillageSetRankCommand extends VillageSubCommand {
    public VillageSetRankCommand(SubCommand parent) {
        super(parent, "setrank <player> <rank>", "rank");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Village village = res.getVillage();
        if (args.length <= 1) {
            getMessage("rank-usage.head").tell(res, village.getReplacements(this,
                    "name", name(),
                    "command", "/" + base + " " + alias,
                    "usage", getUsage(base),
                    "description", getDescription()
            ));
            for (VillageRank rank : VillageRank.values()) {
                if (rank.below(res.getVillageRank())) {
                    getMessage("rank-usage.entry").tell(res,
                            "usage", getUsage(base).replace("<rank>", rank.getTag(res.getVillage())),
                            "rank", rank.getTag(res.getVillage()),
                            "description", getText("rank." + rank.getKey())
                    );
                }
            }
            if (getMessage("rank-usage.footer").exists()) {
                getMessage("rank-usage.footer").tell(res, village.getReplacements(this,
                        "name", name(),
                        "command", "/" + base + " " + alias,
                        "usage", getUsage(base),
                        "description", getDescription()
                ));
            }
            return true;
        }

        Resident tres = village.getResident(args[0]);
        if (tres == null) {
            getError("not-in-current").tell(res, "state", village.getState().getTag());
            return true;
        }

        VillageRank rank = VillageRank.get(args[1]);

        if (rank == null) {
            getError("invalid-rank").tell(res, "rank", args[1]);
        } else if (rank == VillageRank.getChief()) {
            getError("cant-set-chief").tell(res, "base", base, "state", village.getState().getTag());
        } else if (!res.getVillageRank().above(tres.getVillageRank())) {
            getMessage("cant-set-higher").tell(res, "rank", res.getVillageRank().getTag(village));
        } else  if (!res.getVillageRank().above(rank)) {
            getError("rank-required").tell(res, "rank", VillageRank.values().length > rank.ordinal() + 1 ? VillageRank.values()[rank.ordinal() + 1].getTag() : "-");
        } else {
                tres.setVillageRank(rank);
                DatabaseManager.getInstance().saveResident(tres);
                getMessage("success").tell(village.getOnlineResidents(),
                        "rank", rank.getTag(village),
                        "setter", res.getName(),
                        "title", res.getVillageTitle(),
                        "displaytitle", res.getDisplayTitle(),
                        "target", tres.getName());
        }
        return true;
    }
}
