package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.empire.gui.PerksGUI;
import me.crz.minecraft.kingdoms.perk.EmpirePerk;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.resident.Resident;

public class EmpirePerksCommand extends EmpireSubCommand {
    public EmpirePerksCommand(SubCommand parent) {
        super(parent, "perks", "buffs");
        setForceOwnUsage(true);
        registerCommand(new EmpireSubCommand(this, "toggle <perk>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length > 0) {
                    EmpirePerk perk = EmpireManager.getInstance().getPerk(String.join(" ", args));
                    if (perk == null) {
                        getError("unknown-perk").tell(resident, "perk", String.join(" ", args));
                    } else if (!resident.getVillage().getEmpire().hasUnlocked(perk)) {
                        getError("not-unlocked").tell(resident, "perk", perk.getTag());
                    } else if (!perk.hasEffects() && !perk.hasEffects()) {
                        getError("no-effects").tell(resident, "perk", perk.getTag());
                    } else {
                        resident.getVillage().getEmpire().togglePerk(resident.getPlayer(), perk);
                        PerkData perkData = resident.getVillage().getEmpire().getPerkData(perk);
                        getMessage(perkData.isActive() ? "activated" : "deactivated").tell(resident.getVillage().getEmpire().getOnlineResidents(), "perk", perk.getTag());
                    }
                    return true;
                }
                getMessage("usage.head").tell(resident, "name", "Toggle Perks");
                for (EmpirePerk perk : EmpireManager.getInstance().getPerks()) {
                    if (resident.getVillage().getEmpire().hasUnlocked(perk) && perk.hasEffects()) {
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
