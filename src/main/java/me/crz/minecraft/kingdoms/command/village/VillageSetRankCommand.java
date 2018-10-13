package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.entity.Player;

public class VillageSetRankCommand extends VillageSubCommand {
    public VillageSetRankCommand(SubCommand parent) {
        super(parent, "setrank <player> <rank>", VillageRank.ELDER);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length <= 1) {
            res.tell(getMessage("usage.head"), "name", name(), "command", "/" + base + " " + alias, "usage", getUsage(base));
            res.tell(getUsage(base));
            for (VillageRank rank : VillageRank.values()) {
                res.tell(getMessage("usage.entry"),
                        "usage", "/" + base + " " + alias + " " + rank.getTag(res.getVillage()),
                        "description", getText("rank." + rank.getKey())
                );
            }
            if (getMessage("usage.footer").exists()) {
                res.tell(getMessage("usage.footer"), "name", name(), "command", "/" + base + " " + alias);
            }
            return true;
        }
        Player player = res.getPlayer();
        Village village = res.getVillage();
        Utils.runAsync(() -> {
            Resident tres = ResidentManager.getInstance().getResident(args[0]);
            if (tres == null) {
                MessageUtils.tell(player, getError("player-not-found"), "name", args[0]);
                return;
            } else if (!tres.getVillage().equals(village)) {
                MessageUtils.tell(player, getError("not-in-current"), "state", village.getState().getTag());
                return;
            }
            VillageRank rank = null;
            try {
                rank = VillageRank.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                for (VillageRank r : VillageRank.values()) {
                    if (r.getTag(village).equalsIgnoreCase(args[1])) {
                        rank = r;
                        break;
                    }
                }
            }


            if (rank == null) {
                MessageUtils.tell(player, getError("invalid-rank"), "rank", args[1]);
            } else if (rank.equals(VillageRank.CHIEF)) {
                MessageUtils.tell(player, getError("cant-set-chief"), "base", base, "state", village.getState().getTag());
            } else if (tres.getVillageRank().atLeast(res.getVillageRank())) {
                MessageUtils.tell(player, getMessage("cant-set-higher"), "rank", res.getVillageRank().getTag(village));
            } else  if (res.getVillageRank().ordinal() <= rank.ordinal()) {
                MessageUtils.tell(player, getError("rank-required"), "rank", VillageRank.values()[rank.ordinal() + 1].getTag(village));
            } else {
                tres.setVillageRank(rank);
                DatabaseManager.getInstance().saveResident(tres);
                MessageUtils.tellResidents(village.getOnlineResidents(), getMessage("success"),
                        "rank", res.getVillageRank().getTag(village),
                        "setter", player.getName(),
                        "target", tres.getName());
            }
        });
        return true;
    }
}
