package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.perk.SettlementPerk;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.gui.ShopGUI;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;

import java.util.stream.Collectors;

public class SettlementShopCommand extends SettlementSubCommand {
    public SettlementShopCommand(SubCommand parent) {
        super(parent, "shop", "buyperks");
        setForceOwnUsage(true);
        registerCommand(new SettlementSubCommand(this, "unlock <perk>", "buy") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length > 0) {
                    SettlementPerk perk = SettlementManager.getInstance().getPerk(String.join(" ", args));
                    if (perk == null) {
                        getError("unknown-perk").tell(resident, "perk", String.join(" ", args));
                    } else if (!perk.matchesLevelRequirement(resident.getSettlement())) {
                        getError("level-too-low").tell(resident,
                                "perk", perk.getTag(),
                                "state", perk.getState().getTag(),
                                "level", String.valueOf(perk.getLevel())
                        );
                    } else if (resident.getSettlement().hasUnlocked(perk)) {
                        getError("already-unlocked").tell(resident, "perk", perk.getTag());
                    } else if (resident.getSettlement().remove(perk.getUnlockCost())) {
                        resident.getSettlement().unlockPerk(resident, perk);
                        getMessage("success").tell(resident.getSettlement(), "perk", perk.getTag());
                        resident.getSettlement().save();
                        Utils.runAsync(() -> DatabaseManager.getInstance().log(resident.getSettlement(), resident, "perk-unlock", perk.getUnlockCost()));
                    } else {
                        getError("not-possible").tell(resident,
                                "perk", perk.getTag(),
                                "reason", getText("missing-items") + "\n"
                                + perk.getUnlockCost().entrySet().stream()
                                .filter(e -> !resident.getSettlement().has(e.getKey(), e.getValue()))
                                .map(e -> getMessage("entry").getReplacedText(
                                        "material", e.getKey(),
                                        "amount", String.valueOf(resident.getSettlement().getAmount(e.getKey()))))
                                .collect(Collectors.joining("\n"))
                        );
                    }
                    return true;
                }
                getMessage("usage.head").tell(resident, "name", "Unlock Perks");
                for (SettlementPerk perk : SettlementManager.getInstance().getPerks()) {
                    if (!resident.getSettlement().hasUnlocked(perk) && perk.matchesLevelRequirement(resident.getSettlement())) {
                        getMessage("usage.entry").tell(resident, "usage", getUsage(base).replace("<perk>", perk.name()), "description", perk.getTag());
                    }
                }
                return false;
            }
        });
        registerCommand(new SettlementSubCommand(this, "repeat <perk>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length > 0) {
                    SettlementPerk perk = SettlementManager.getInstance().getPerk(String.join(" ", args));
                    if (perk == null) {
                        getError("unknown-perk").tell(resident, "perk", String.join(" ", args));
                    } else if (!resident.getSettlement().hasUnlocked(perk)) {
                        getError("not-unlocked").tell(resident, "perk", perk.getTag());
                    } else if (!perk.matchesLevelRequirement(resident.getSettlement())) {
                        getError("level-too-low").tell(resident,
                                "perk", perk.getTag(),
                                "state", perk.getState().getTag(),
                                "level", String.valueOf(perk.getLevel())
                        );
                    } else if (!resident.getSettlement().checkPerkCount(perk)) {
                        getError("already-repeated").tell(resident, "perk", perk.getTag(), "amount", String.valueOf(perk.getRepeat()));
                    } else if (resident.getSettlement().remove(perk.getRepeatCost())) {
                        resident.getSettlement().repeatPerk(resident.getPlayer(), perk);
                        MessageUtils.tellResidents(resident.getSettlement().getOnlineResidents(), getMessage("success").getReplacedComponent("perk", perk.getTag()));
                        DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
                        Utils.runAsync(() -> DatabaseManager.getInstance().log(resident.getSettlement(), resident, "perk-repeat", perk.getRepeatCost()));
                    } else {
                        getError("not-possible").tell(resident,
                                "perk", perk.getTag(),
                                "reason", getText("missing-items") + "\n"
                                + perk.getUnlockCost().entrySet().stream()
                                .filter(e -> !resident.getSettlement().has(e.getKey(), e.getValue()))
                                .map(e -> getMessage("entry").getReplacedText(
                                        "material", e.getKey(),
                                        "amount", String.valueOf(resident.getSettlement().getAmount(e.getKey()))))
                                .collect(Collectors.joining("\n"))
                        );
                    }
                    return true;
                }
                getMessage("usage.head").tell(resident, "name", "Repeat Perks");
                for (SettlementPerk perk : SettlementManager.getInstance().getPerks()) {
                    if (resident.getSettlement().hasUnlocked(perk) && resident.getSettlement().checkPerkCount(perk)) {
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
            new ShopGUI(resident).open();
            return true;
        }
        return false;
    }
}
