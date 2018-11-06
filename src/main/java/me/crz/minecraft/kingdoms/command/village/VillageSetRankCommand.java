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
        super(parent, "setrank <player> <rank>", VillageRank.ELDER, "rank");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length <= 1) {
            getMessage("usage.head").tell(res,
                    "name", name(),
                    "command", "/" + base + " " + alias,
                    "usage", getUsage(base),
                    "aliases", getAliases().isEmpty() ? "" : getMessage("usage.aliases").getReplacedText("aliases", String.join(", ", getAliases())),
                    "description", getText("description")
            );
            for (VillageRank rank : VillageRank.values()) {
                if (rank.below(res.getVillageRank())) {
                    getMessage("usage.entry").tell(res,
                            "usage", getUsage(base).replace("<rank>", rank.getTag(res.getVillage())),
                            "rank", rank.getTag(res.getVillage()),
                            "description", getText("rank." + rank.getKey())
                    );
                }
            }
            if (getMessage("usage.footer").exists()) {
                getMessage("usage.footer").tell(res,
                        "name", name(),
                        "command", "/" + base + " " + alias, "usage",
                        getUsage(base),
                        "aliases", getAliases().isEmpty() ? "" : getMessage("usage.aliases").getReplacedText("aliases", String.join(", ", getAliases())),
                        "description", getText("description")
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
            } else if (tres.getVillageRank().atLeast(res.getVillageRank())) {
                getMessage("cant-set-higher").tell(player, "rank", res.getVillageRank().getTag(village));
            } else  if (rank.atLeast(res.getVillageRank())) {
                getMessage("rank-required").tell(player, "rank", VillageRank.values()[rank.ordinal() - 1].getTag(village));
            } else {
                tres.setVillageRank(rank);
                DatabaseManager.getInstance().saveResident(tres);
                getMessage("success").tell(village.getOnlineResidents(),
                        "rank", rank.getTag(village),
                        "setter", res.getName(),
                        "title", res.getTitle(),
                        "target", tres.getName());
            }
        });
        return true;
    }
}
