package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.EmpireSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;

public class EmpireSetRankCommand extends EmpireSubCommand {
    public EmpireSetRankCommand(SubCommand parent) {
        super(parent, "setrank <player> <rank>", EmpireRank.LEADER, "rank");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length <= 1) {
            res.tell(getMessage("usage.head"), "name", name(), "command", "/" + base + " " + alias, "usage", getUsage(base));
            res.tell(getUsage(base));
            for (EmpireRank rank : EmpireRank.values()) {
                res.tell(getMessage("usage.entry"),
                        "usage", "/" + base + " " + alias + " " + rank.getTag(),
                        "description", getText("rank." + rank.getKey())
                );
            }
            if (getMessage("usage.footer").exists()) {
                res.tell(getMessage("usage.footer"), "name", name(), "command", "/" + base + " " + alias);
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
            } else if (!empire.equals(tres.getVillage().getEmpire())) {
                getError("not-in-empire").tell(player);
                return;
            }

            EmpireRank rank = EmpireRank.get(args[1]);

            if (rank == null) {
                getError("invalid-rank").tell(player, "rank", args[1]);
            } else if (rank.equals(EmpireRank.LEADER)) {
                getError("cant-set-leader").tell(player, "base", base);
            } else if (tres.getEmpireRank().atLeast(res.getEmpireRank())) {
                getMessage("cant-set-higher").tell(player, "rank", res.getEmpireRank().getTag());
            } else  if (res.getEmpireRank().ordinal() <= rank.ordinal()) {
                getError("rank-required").tell(player, "rank", EmpireRank.values()[rank.ordinal() + 1].getTag());
            } else {
                tres.setEmpireRank(rank);
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
