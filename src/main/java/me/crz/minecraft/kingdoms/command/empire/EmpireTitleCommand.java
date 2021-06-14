package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;

import java.util.Collection;

public class EmpireTitleCommand extends EmpireSubCommand {
    public EmpireTitleCommand(SubCommand parent) {
        super(parent, "title <title>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            Collection<String> titles = res.getVillage().getEmpire().getTitles(res.getEmpireRank());
            res.tell(getMessage("usage.head"), "name", name(), "command", "/" + base + " " + alias, "usage", getUsage(base), "type", "");
            res.tell(getMessage("usage.entry"), "usage", getUsage(base));
            String defaultTitle = res.getVillage().getEmpire().getDefaultTitle(res.getEmpireRank());
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
            message.tell(res.getVillage().getEmpire().getOnlineResidents(),
                    "rank", res.getEmpireRank().getTag(res.getVillage().getEmpire()),
                    "title", res.getEmpireTitle(),
                    "name", res.getName(),
                    "empirename", res.getVillage().getEmpire().getName(),
                    "empirestate", res.getVillage().getEmpire().getStateTag()
            );
        }
        return true;
    }
}
