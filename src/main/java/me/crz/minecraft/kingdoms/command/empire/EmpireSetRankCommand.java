package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;

public class EmpireSetRankCommand extends EmpireSubCommand {
    public EmpireSetRankCommand(SubCommand parent) {
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
            for (EmpireRank rank : EmpireRank.values()) {
                if (rank.below(res.getVillageRank())) {
                    getMessage("rank-usage.entry").tell(res,
                            "usage", getUsage(base).replace("<rank>", rank.getTag()),
                            "rank", rank.getTag(),
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
        Empire empire = res.getVillage().getEmpire();
        Utils.runAsync(() -> {
            Resident tres = ResidentManager.getInstance().getResident(args[0]);
            if (tres == null) {
                getError("player-not-found").tell(player, "name", args[0]);
                return;
            } else if (!empire.isMember(tres)) {
                getError("not-in-empire").tell(player);
                return;
            }

            EmpireRank rank = EmpireRank.get(args[1]);

            if (rank == null) {
                getError("invalid-rank").tell(player, "rank", args[1]);
            } else if (rank.equals(EmpireRank.getLeader())) {
                getError("cant-set-leader").tell(player, "base", base);
            } else if (!res.getEmpireRank().above(tres.getEmpireRank())) {
                getMessage("cant-set-higher").tell(player, "rank", res.getEmpireRank().getTag());
            } else  if (!res.getEmpireRank().above(rank)) {
                getError("rank-required").tell(player, "rank", EmpireRank.values().length > rank.ordinal() + 1 ? EmpireRank.values()[rank.ordinal() + 1].getTag() : "-");
            } else {
                tres.setEmpireRank(rank);
                if (rank.name().equalsIgnoreCase("senator")) {
                    empire.addSenator(tres);
                } else {
                    empire.removeSenator(tres);
                }
                DatabaseManager.getInstance().saveResident(tres);
                MessageUtils.tellResidents(empire.getOnlineResidents(), getMessage("success"),
                        "rank", rank.getTag(),
                        "title", res.getTitle(),
                        "setter", res.getName(),
                        "target", tres.getName());
            }
        });
        return true;
    }
}
