package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Collection;

public class VillageTitleCommand extends VillageSubCommand {
    public VillageTitleCommand(SubCommand parent) {
        super(parent, "title <title>", VillageRank.RESIDENT);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            Collection<String> titles = res.getVillageRank().getTitles(res.getVillage());
            res.tell(getMessage("usage.head"), "name", name(), "command", "/" + base + " " + alias, "usage", getUsage(base));
            res.tell(getUsage(base));
            for (String title : titles) {
                res.tell(getMessage("usage.entry"), "usage", "/" + base + " " + alias + " " + title);
            }
            if (getMessage("usage.footer").exists()) {
                res.tell(getMessage("usage.footer"), "name", name(), "command", "/" + base + " " + alias);
            }
            return true;
        }
        if (!res.setTitle(args[0])) {
            getError("invalid-title").tell(res, "title", args[0]);
        } else {
            Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
            Message message = getMessage("success." + res.getVillage().getState().getKey() + "." + res.getVillageRank().getKey());
            if (!message.exists()) {
                message = getMessage("success.default");
            }
            MessageUtils.tellResidents(res.getVillage().getOnlineResidents(), message,
                    "rank", res.getVillageRank().getTag(res.getVillage()),
                    "title", res.getTitle(),
                    "name", res.getName(),
                    "villagename", res.getVillage().getName(),
                    "villagestate", res.getVillage().getState().getTag()
            );
        }
        return true;
    }
}
