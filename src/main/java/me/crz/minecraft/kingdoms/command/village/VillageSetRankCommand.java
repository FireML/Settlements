package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.entity.Player;

public class VillageSetRankCommand extends VillageSubCommand {
    public VillageSetRankCommand(SubCommand parent) {
        super(parent, "setrank <player> <rank>", "rank");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length <= 1) {
            getMessage("rank-usage.head").tell(res,
                    "name", name(),
                    "command", "/" + base + " " + alias,
                    "usage", getUsage(base),
                    "description", getDescription()
            );
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
                getMessage("rank-usage.footer").tell(res,
                        "name", name(),
                        "command", "/" + base + " " + alias,
                        "usage", getUsage(base),
                        "description", getDescription()
                );
            }
            return true;
        }
        Player player = res.getPlayer();
        Village village = res.getVillage();
        Utils.runAsync(() -> {
            Resident tres = ResidentManager.getInstance().getResident(args[0]);
            if (tres == null) {
                getError("player-not-found").tell(player, "name", args[0]);
                return;
            } else if (!tres.getVillage().equals(village)) {
                getError("not-in-current").tell(player, "state", village.getState().getTag());
                return;
            }

            VillageRank rank = VillageRank.get(args[1]);

            if (rank == null) {
                getError("invalid-rank").tell(player, "rank", args[1]);
            } else if (rank == VillageRank.CHIEF) {
                getError("cant-set-chief").tell(player, "base", base, "state", village.getState().getTag());
            } else if (!res.getVillageRank().above(tres.getVillageRank())) {
                getMessage("cant-set-higher").tell(player, "rank", res.getVillageRank().getTag(village));
            } else  if (!res.getVillageRank().above(rank)) {
                getError("rank-required").tell(player, "rank", VillageRank.values().length > rank.ordinal() + 1 ? VillageRank.values()[rank.ordinal() + 1].getTag() : "-");
            } else {
                Utils.runSync(() -> {
                    tres.setVillageRank(rank);
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(tres));
                    getMessage("success").tell(village.getOnlineResidents(),
                            "rank", rank.getTag(village),
                            "setter", res.getName(),
                            "title", res.getTitle(),
                            "target", tres.getName());
                });
            }
        });
        return true;
    }
}
