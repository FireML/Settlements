package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.utils.Message;

import java.util.Collection;

public class EmpireTitleCommand extends EmpireSubCommand {
    public EmpireTitleCommand(SubCommand parent) {
        super(parent, "title <title>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            Collection<String> titles = res.getSettlement().getEmpire().getTitles(res.getEmpireRank());
            res.tell(getMessage("usage.head"), "name", name(), "command", "/" + base + " " + alias, "usage", getUsage(base), "type", "");
            res.tell(getMessage("usage.entry"), "usage", getUsage(base));
            String defaultTitle = res.getSettlement().getEmpire().getDefaultTitle(res.getEmpireRank());
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
        if (!res.setEmpireTitle(args[0])) {
            getError("invalid-title").tell(res, "title", args[0]);
        } else {
            DatabaseManager.getInstance().saveResident(res);
            Message message = getMessage("title.success." + res.getEmpireRank().getKey());
            if (!message.exists()) {
                message = getMessage("title.success.default");
            }
            message.tell(res.getSettlement().getEmpire().getOnlineResidents(),
                    "rank", res.getEmpireRank().getTag(res.getSettlement().getEmpire()),
                    "title", res.getEmpireTitle(),
                    "name", res.getName(),
                    "empirename", res.getSettlement().getEmpire().getName(),
                    "empirestate", res.getSettlement().getEmpire().getStateTag()
            );
        }
        return true;
    }
}
