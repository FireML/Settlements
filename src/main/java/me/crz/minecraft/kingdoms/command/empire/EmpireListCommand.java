package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageAccess;
import org.bukkit.entity.Player;

public class EmpireListCommand extends SubCommand {
    public EmpireListCommand(SubCommand parent) {
        super(parent, "list [<page>]");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        int page = 1;
        VillageAccess access = null;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (IllegalArgumentException e1) {
                getError("not-a-number").tell(player, "input", args[0]);
                return false;
            }
        }
        if (page < 1) {
            getError("not-a-number").tell(player, "input", String.valueOf(page));
            return false;
        }
        int finalPage = page;
        Utils.runAsync(() -> {
            getMessage("head").tell(player, "page", String.valueOf(finalPage));
            EmpireManager.getInstance().getEmpires().stream()
                    .sorted(Empire::compareTo)
                    .skip((finalPage - 1) * 20)
                    .forEachOrdered(e -> {
                        Village capital = e.getCapital();
                        Resident chief = capital.getChief();
                        getMessage("entry").tell(player,
                                "name", e.getName(),
                                "level", String.valueOf(e.getLevel()),
                                "description", e.getDescription(),
                                "members", String.valueOf(e.getMemberIds().size()),
                                "capital", capital.getName(),
                                "capitalstate", capital.getState().getTag(),
                                "chief", chief.getName(),
                                "chieftitle", chief.getTitle(),
                                "chiefrank", VillageRank.CHIEF.getTag(capital)
                        );
                    });
        });

        return true;
    }
}
