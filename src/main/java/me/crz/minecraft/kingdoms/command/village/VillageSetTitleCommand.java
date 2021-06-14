package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;

import java.util.Collection;

public class VillageSetTitleCommand extends VillageSubCommand {
    public VillageSetTitleCommand(SubCommand parent) {
        super(parent, "settitle <player> <title>");
        setVisible(false);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length == 0) {
            return false;
        }

        Resident target = res.getVillage().getResident(args[0]);
        if (target == null) {
            getError("not-in-current").tell(res, "state", res.getVillage().getState().getTag());
        } else if (args.length == 1) {
            Collection<String> titles = res.getVillage().getTitles(target.getVillageRank());
            res.tell(getMessage("usage.head"), "name", name(), "command", "/" + base + " " + alias, "usage", getUsage(base), "type", "");
            res.tell(getMessage("usage.entry"), "usage", getUsage(base));
            String defaultTitle = res.getVillage().getDefaultTitle(target.getVillageRank());
            res.tell(getMessage("usage.entry"), "usage", "/" + base + " " + alias + " " + defaultTitle);
            for (String title : titles) {
                if (!title.equals(defaultTitle)) {
                    res.tell(getMessage("usage.entry"), "usage", "/" + base + " " + alias + " " + title);
                }
            }
            if (getMessage("usage.footer").exists()) {
                res.tell(getMessage("usage.footer"), "name", name(), "command", "/" + base + " " + alias);
            }
        } else if (!res.getVillageRank().above(target.getVillageRank())) {
            getMessage("cant-set-higher").tell(res, "rank", res.getVillageRank().getTag(res.getVillage()));
        } else if (!target.setVillageTitle(args[1])) {
            getError("invalid-title").tell(res, "title", args[1]);
        } else {
            DatabaseManager.getInstance().saveResident(target);
            getMessage("success").tell(res.getVillage().getOnlineResidents(),
                    "rank", target.getVillageRank().getTag(res.getVillage()),
                    "title", target.getVillageTitle(),
                    "target", target.getName(),
                    "sendertitle", res.getVillageTitle(),
                    "senderdisplaytitle", res.getDisplayTitle(),
                    "sender", res.getName(),
                    "villagename", res.getVillage().getName(),
                    "villagestate", res.getVillage().getState().getTag()
            );
        }
        return true;
    }
}
