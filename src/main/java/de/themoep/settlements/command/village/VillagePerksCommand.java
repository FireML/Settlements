package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.SettlementManager;
import de.themoep.settlements.village.gui.PerksGUI;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.perk.LevelPerk;
import de.themoep.settlements.perk.PerkData;

public class VillagePerksCommand extends VillageSubCommand {
    public VillagePerksCommand(SubCommand parent) {
        super(parent, "perks", "buffs");
        setForceOwnUsage(true);
        registerCommand(new VillageSubCommand(this, "toggle <perk>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length > 0) {
                    LevelPerk perk = SettlementManager.getInstance().getPerk(String.join(" ", args));
                    if (perk == null) {
                        getError("unknown-perk").tell(resident, "perk", String.join(" ", args));
                    } else if (!resident.getSettlement().hasUnlocked(perk)) {
                        getError("not-unlocked").tell(resident, "perk", perk.getTag());
                    } else if (!perk.hasEffects() && !perk.hasEffects()) {
                        getError("no-effects").tell(resident, "perk", perk.getTag());
                    } else {
                        resident.getSettlement().togglePerk(resident.getPlayer(), perk);
                        PerkData perkData = resident.getSettlement().getPerkData(perk);
                        getMessage(perkData.isActive() ? "activated" : "deactivated").tell(resident.getSettlement().getOnlineResidents(), "perk", perk.getTag());
                    }
                    return true;
                }
                getMessage("usage.head").tell(resident, "name", "Toggle Perks");
                for (LevelPerk perk : SettlementManager.getInstance().getPerks()) {
                    if (resident.getSettlement().hasUnlocked(perk) && perk.hasEffects()) {
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
