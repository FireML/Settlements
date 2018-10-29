package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
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
            getMessage("usage.head").tell(res, "name", name(), "command", "/" + base + " " + alias, "usage", getUsage(base));
            res.tell(getUsage(base));
            for (VillageRank rank : VillageRank.values()) {
                getMessage("usage.entry").tell(res,
                        "usage", "/" + base + " " + alias + " " + rank.getTag(res.getVillage()),
                        "description", getText("rank." + rank.getKey())
                );
            }
            if (getMessage("usage.footer").exists()) {
                getMessage("usage.footer").tell(res, "name", name(), "command", "/" + base + " " + alias);
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
                        "rank", res.getVillageRank().getTag(village),
                        "setter", player.getName(),
                        "target", tres.getName());
            }
        });
        return true;
    }
}
