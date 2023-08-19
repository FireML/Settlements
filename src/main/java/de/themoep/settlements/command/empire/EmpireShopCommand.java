package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.empire.gui.ShopGUI;
import de.themoep.settlements.perk.EmpirePerk;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;

import java.util.stream.Collectors;

public class EmpireShopCommand extends EmpireSubCommand {
    public EmpireShopCommand(SubCommand parent) {
        super(parent, "shop", "buyperks");
        setForceOwnUsage(true);
        registerCommand(new EmpireSubCommand(this, "unlock <perk>", "buy") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length > 0) {
                    EmpirePerk perk = EmpireManager.getInstance().getPerk(String.join(" ", args));
                    if (perk == null) {
                        getError("unknown-perk").tell(resident, "perk", String.join(" ", args));
                    } else if (!perk.matchesLevelRequirement(resident.getSettlement().getEmpire())) {
                        getError("level-too-low").tell(resident,
                                "perk", perk.getTag(),
                                "state", resident.getSettlement().getEmpire().getStateTag(),
                                "level", String.valueOf(perk.getLevel())
                        );
                    } else if (resident.getSettlement().getEmpire().hasUnlocked(perk)) {
                        getError("already-unlocked").tell(resident, "perk", perk.getTag());
                    } else if (resident.getSettlement().remove(perk.getUnlockCost())) {
                        resident.getSettlement().getEmpire().unlockPerk(resident, perk);
                        getMessage("success").tell(resident.getSettlement().getEmpire(), "perk", perk.getTag());
                        resident.getSettlement().getEmpire().save();
                        Utils.runAsync(() -> {
                            DatabaseManager.getInstance().log(resident.getSettlement().getEmpire(), resident, "perk-unlock", perk.getUnlockCost());
                        });
                    } else {
                        getError("not-possible").tell(resident,
                                "perk", perk.getTag(),
                                "reason", getText("missing-items") + "\n"
                                + perk.getUnlockCost().entrySet().stream()
                                .filter(e -> !resident.getSettlement().getEmpire().has(e.getKey(), e.getValue()))
                                .map(e -> getMessage("entry").getReplacedText(
                                        "material", e.getKey(),
                                        "amount", String.valueOf(resident.getSettlement().getEmpire().getAmount(e.getKey()))))
                                .collect(Collectors.joining("\n"))
                        );
                    }
                    return true;
                }
                getMessage("usage.head").tell(resident, "name", "Unlock Perks");
                for (EmpirePerk perk : EmpireManager.getInstance().getPerks()) {
                    if (!resident.getSettlement().getEmpire().hasUnlocked(perk) && perk.matchesLevelRequirement(resident.getSettlement().getEmpire())) {
                        getMessage("usage.entry").tell(resident, "usage", getUsage(base).replace("<perk>", perk.name()), "description", perk.getTag());
                    }
                }
                return false;
            }
        });
        registerCommand(new EmpireSubCommand(this, "repeat <perk>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length > 0) {
                    EmpirePerk perk = EmpireManager.getInstance().getPerk(String.join(" ", args));
                    if (perk == null) {
                        getError("unknown-perk").tell(resident, "perk", String.join(" ", args));
                    } else if (!resident.getSettlement().getEmpire().hasUnlocked(perk)) {
                        getError("not-unlocked").tell(resident, "perk", perk.getTag());
                    } else if (!perk.matchesLevelRequirement(resident.getSettlement().getEmpire())) {
                        getError("level-too-low").tell(resident,
                                "perk", perk.getTag(),
                                "state", resident.getSettlement().getEmpire().getStateTag(),
                                "level", String.valueOf(perk.getLevel())
                        );
                    } else if (!resident.getSettlement().getEmpire().checkPerkCount(perk)) {
                        getError("already-repeated").tell(resident, "perk", perk.getTag(), "amount", String.valueOf(perk.getRepeat()));
                    } else if (resident.getSettlement().getEmpire().remove(perk.getRepeatCost())) {
                        resident.getSettlement().getEmpire().repeatPerk(resident.getPlayer(), perk);
                        MessageUtils.tellResidents(resident.getSettlement().getEmpire().getOnlineResidents(), getMessage("success"), "perk", perk.getTag());
                        DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
                        Utils.runAsync(() -> DatabaseManager.getInstance().log(resident.getSettlement().getEmpire(), resident, "perk-repeat", perk.getRepeatCost()));
                    } else {
                        getError("not-possible").tell(resident,
                                "perk", perk.getTag(),
                                "reason", getText("missing-items") + "\n"
                                + perk.getUnlockCost().entrySet().stream()
                                .filter(e -> !resident.getSettlement().getEmpire().has(e.getKey(), e.getValue()))
                                .map(e -> getMessage("entry").getReplacedText(
                                        "material", e.getKey(),
                                        "amount", String.valueOf(resident.getSettlement().getEmpire().getAmount(e.getKey()))))
                                .collect(Collectors.joining("\n"))
                        );
                    }
                    return true;
                }
                getMessage("usage.head").tell(resident, "name", "Repeat Perks");
                for (EmpirePerk perk : EmpireManager.getInstance().getPerks()) {
                    if (resident.getSettlement().getEmpire().hasUnlocked(perk) && resident.getSettlement().getEmpire().checkPerkCount(perk)) {
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
