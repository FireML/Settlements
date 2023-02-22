package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.perk.PerkData;
import de.themoep.settlements.perk.SettlementPerk;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.gui.PerksGUI;

public class SettlementPerksCommand extends SettlementSubCommand {
    public SettlementPerksCommand(SubCommand parent) {
        super(parent, "perks", "buffs");
        setForceOwnUsage(true);
        registerCommand(new SettlementSubCommand(this, "toggle <perk>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length > 0) {
                    SettlementPerk perk = SettlementManager.getInstance().getPerk(String.join(" ", args));
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
                for (SettlementPerk perk : SettlementManager.getInstance().getPerks()) {
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
