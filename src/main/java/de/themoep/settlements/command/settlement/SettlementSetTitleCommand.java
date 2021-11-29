package de.themoep.settlements.command.settlement;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;

import java.util.Collection;

public class SettlementSetTitleCommand extends SettlementSubCommand {
    public SettlementSetTitleCommand(SubCommand parent) {
        super(parent, "settitle <player> <title>");
        setVisible(false);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length == 0) {
            return false;
        }

        Resident target = res.getSettlement().getResident(args[0]);
        if (target == null) {
            getError("not-in-current").tell(res, "state", res.getSettlement().getState().getTag());
        } else if (args.length == 1) {
            Collection<String> titles = res.getSettlement().getTitles(target.getSettlementRank());
            res.tell(getMessage("usage.head"), "name", name(), "command", "/" + base + " " + alias, "usage", getUsage(base), "type", "");
            res.tell(getMessage("usage.entry"), "usage", getUsage(base));
            String defaultTitle = res.getSettlement().getDefaultTitle(target.getSettlementRank());
            res.tell(getMessage("usage.entry"), "usage", "/" + base + " " + alias + " " + defaultTitle);
            for (String title : titles) {
                if (!title.equals(defaultTitle)) {
                    res.tell(getMessage("usage.entry"), "usage", "/" + base + " " + alias + " " + title);
                }
            }
            if (getMessage("usage.footer").exists()) {
                res.tell(getMessage("usage.footer"), "name", name(), "command", "/" + base + " " + alias);
            }
        } else if (!res.getSettlementRank().above(target.getSettlementRank())) {
            getMessage("cant-set-higher").tell(res, "rank", res.getSettlementRank().getTag(res.getSettlement()));
        } else if (!target.setSettlementTitle(args[1])) {
            getError("invalid-title").tell(res, "title", args[1]);
        } else {
            DatabaseManager.getInstance().saveResident(target);
            getMessage("success").tell(res.getSettlement().getOnlineResidents(),
                    "rank", target.getSettlementRank().getTag(res.getSettlement()),
                    "title", target.getSettlementTitle(),
                    "target", target.getName(),
                    "sendertitle", res.getSettlementTitle(),
                    "senderdisplaytitle", res.getDisplayTitle(),
                    "sender", res.getName(),
                    "settlementname", res.getSettlement().getName(),
                    "settlementstate", res.getSettlement().getState().getTag()
            );
        }
        return true;
    }
}
