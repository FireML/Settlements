package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;

import java.util.Collection;

public class VillageTitleCommand extends VillageSubCommand {
    public VillageTitleCommand(SubCommand parent) {
        super(parent, "title <title>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            Collection<String> titles = res.getSettlement().getTitles(res.getSettlementRank());
            res.tell(getMessage("usage.head"), "name", name(), "command", "/" + base + " " + alias, "usage", getUsage(base), "type", "");
            res.tell(getMessage("usage.entry"), "usage", getUsage(base));
            String defaultTitle = res.getSettlement().getDefaultTitle(res.getSettlementRank());
            res.tell(getMessage("usage.entry"), "usage", "/" + base + " " + alias + " " + defaultTitle);
            for (String title : titles) {
                if (!title.equals(defaultTitle)) {
                    res.tell(getMessage("usage.entry"), "usage", "/" + base + " " + alias + " " + title);
                }
            }
            if (getMessage("usage.footer").exists()) {
                res.tell(getMessage("usage.footer"), "name", name(), "command", "/" + base + " " + alias);
            }
            return true;
        }
        if (!res.setSettlementTitle(args[0])) {
            getError("invalid-title").tell(res, "title", args[0]);
        } else {
            DatabaseManager.getInstance().saveResident(res);
            Message message = getMessage("success." + res.getSettlement().getState().getKey() + "." + res.getSettlementRank().getKey());
            if (!message.exists()) {
                message = getMessage("success.default");
            }
            message.tell(res.getSettlement().getOnlineResidents(),
                    "rank", res.getSettlementRank().getTag(res.getSettlement()),
                    "title", res.getSettlementTitle(),
                    "name", res.getName(),
                    "settlementname", res.getSettlement().getName(),
                    "settlementstate", res.getSettlement().getState().getTag()
            );
        }
        return true;
    }
}
