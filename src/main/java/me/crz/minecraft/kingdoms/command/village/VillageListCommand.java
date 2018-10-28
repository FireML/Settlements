package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageAccess;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

public class VillageListCommand extends SubCommand {
    public VillageListCommand(SubCommand parent) {
        super(parent, "list [open] [<page>]");
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
                if ("open".equalsIgnoreCase(args[0])) {
                    access = VillageAccess.OPEN;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (IllegalArgumentException e2) {
                            getError("not-a-number").tell(player, "input", args[1]);
                            return false;
                        }
                    }
                } else {
                    getError("not-a-number").tell(player, "input", args[0]);
                    return false;
                }
            }
        }
        if (page < 1) {
            getError("not-a-number").tell(player, "input", String.valueOf(page));
            return false;
        }
        VillageAccess finalAccess = access;
        int finalPage = page;
        Utils.runAsync(() -> {
            getMessage("head").tell(player,
                    "access", finalAccess != null ? finalAccess.getTag() : "",
                    "page", String.valueOf(finalPage)
            );
            VillageManager.getInstance().getVillages().stream()
                    .filter(v -> v.getAccess().atLeast(finalAccess))
                    .sorted(Village::compareTo)
                    .skip((finalPage - 1) * 20)
                    .forEachOrdered(v -> {
                        Resident chief = ResidentManager.getInstance().getResident(v.getChief());
                        getMessage("entry").tell(player,
                                "name", v.getName(),
                                "level", String.valueOf(v.getLevel()),
                                "description", v.getDescription(),
                                "state", v.getState().getTag(),
                                "access", v.getAccess() != VillageAccess.PRIVATE ? v.getAccess().getTag() : "",
                                "residents", String.valueOf(v.getResidentIds().size()),
                                "chief", chief.getName(),
                                "chieftitle", chief.getTitle(),
                                "chiefrank", VillageRank.CHIEF.getTag(v)
                        );
                    });
        });

        return true;
    }
}
