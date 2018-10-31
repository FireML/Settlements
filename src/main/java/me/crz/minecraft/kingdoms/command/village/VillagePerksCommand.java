package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.village.gui.PerksGUI;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.perk.VillagePerk;

import java.util.stream.Collectors;

public class VillagePerksCommand extends VillageSubCommand {
    public VillagePerksCommand(SubCommand parent) {
        super(parent, "perks", VillageRank.RESIDENT, "buffs");
        setPermission("kingdoms.user");
        setForceOwnUsage(true);
        registerCommand(new VillageSubCommand(this, "toggle <perk>", VillageRank.ELDER) {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length > 0) {
                    VillagePerk perk = VillageManager.getInstance().getPerk(String.join(" ", args));
                    if (perk == null) {
                        getError("unknown-perk").tell(resident, "perk", String.join(" ", args));
                    } else if (!resident.getVillage().hasUnlocked(perk)) {
                        getError("not-unlocked").tell(resident, "perk", perk.getTag());
                    } else if (!perk.hasEffects()) {
                        getError("no-effects").tell(resident, "perk", perk.getTag());
                    } else {
                        PerkData perkData = resident.getVillage().getPerkData(perk);
                        perkData.toggle();
                        MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage(perkData.isActive() ? "activated" : "deactivated"), "perk", perk.getTag());
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
                    }
                    return true;
                }
                getMessage("usage.head").tell(resident, "name", "Toggle Perks");
                for (VillagePerk perk : VillageManager.getInstance().getPerks()) {
                    if (resident.getVillage().hasUnlocked(perk) && perk.hasEffects()) {
                        getMessage("usage.entry").tell(resident, "usage", getUsage(base) + " " + perk.name(), "description", perk.getTag());
                    }
                }
                return false;
            }
        });

    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length == 0) {
            new PerksGUI(resident).open();
            return true;
        }
        return false;
    }
}
