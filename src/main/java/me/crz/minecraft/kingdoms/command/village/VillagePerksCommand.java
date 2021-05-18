package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.perk.LevelPerk;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.gui.PerksGUI;

public class VillagePerksCommand extends VillageSubCommand {
    public VillagePerksCommand(SubCommand parent) {
        super(parent, "perks", "buffs");
        setForceOwnUsage(true);
        registerCommand(new VillageSubCommand(this, "toggle <perk>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length > 0) {
                    LevelPerk perk = VillageManager.getInstance().getPerk(String.join(" ", args));
                    if (perk == null) {
                        getError("unknown-perk").tell(resident, "perk", String.join(" ", args));
                    } else if (!resident.getVillage().hasUnlocked(perk)) {
                        getError("not-unlocked").tell(resident, "perk", perk.getTag());
                    } else if (!perk.hasEffects() && !perk.hasEffects()) {
                        getError("no-effects").tell(resident, "perk", perk.getTag());
                    } else {
                        resident.getVillage().togglePerk(resident.getPlayer(), perk);
                        PerkData perkData = resident.getVillage().getPerkData(perk);
                        getMessage(perkData.isActive() ? "activated" : "deactivated").tell(resident.getVillage().getOnlineResidents(), "perk", perk.getTag());
                    }
                    return true;
                }
                getMessage("usage.head").tell(resident, "name", "Toggle Perks");
                for (LevelPerk perk : VillageManager.getInstance().getPerks()) {
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
