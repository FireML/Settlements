package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.village.gui.ShopGUI;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.perk.LevelPerk;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;

import java.util.stream.Collectors;

public class VillageShopCommand extends VillageSubCommand {
    public VillageShopCommand(SubCommand parent) {
        super(parent, "shop", "buyperks");
        setForceOwnUsage(true);
        registerCommand(new VillageSubCommand(this, "unlock <perk>", "buy") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length > 0) {
                    LevelPerk perk = VillageManager.getInstance().getPerk(String.join(" ", args));
                    if (perk == null) {
                        getError("unknown-perk").tell(resident, "perk", String.join(" ", args));
                    } else if (!perk.matchesLevelRequirement(resident.getVillage())) {
                        getError("level-too-low").tell(resident,
                                "perk", perk.getTag(),
                                "state", perk.getState().getTag(),
                                "level", String.valueOf(perk.getLevel())
                        );
                    } else if (resident.getVillage().hasUnlocked(perk)) {
                        getError("already-unlocked").tell(resident, "perk", perk.getTag());
                    } else if (resident.getVillage().remove(perk.getUnlockCost())) {
                        resident.getVillage().unlockPerk(resident.getPlayer(), perk);
                        MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("success"), "perk", perk.getTag());
                        DatabaseManager.getInstance().saveSettlement(resident.getVillage());
                        Utils.runAsync(() -> DatabaseManager.getInstance().log(resident.getVillage(), resident, "perk-unlock", perk.getUnlockCost()));
                    } else {
                        getError("not-possible").tell(resident,
                                "perk", perk.getTag(),
                                "reason", getText("missing-items") + "\n"
                                + perk.getUnlockCost().entrySet().stream()
                                .filter(e -> !resident.getVillage().has(e.getKey(), e.getValue()))
                                .map(e -> getMessage("entry").getReplacedText(
                                        "material", e.getKey(),
                                        "amount", String.valueOf(resident.getVillage().getAmount(e.getKey()))))
                                .collect(Collectors.joining("\n"))
                        );
                    }
                    return true;
                }
                getMessage("usage.head").tell(resident, "name", "Unlock Perks");
                for (LevelPerk perk : VillageManager.getInstance().getPerks()) {
                    if (!resident.getVillage().hasUnlocked(perk) && perk.matchesLevelRequirement(resident.getVillage())) {
                        getMessage("usage.entry").tell(resident, "usage", getUsage(base).replace("<perk>", perk.name()), "description", perk.getTag());
                    }
                }
                return false;
            }
        });
        registerCommand(new VillageSubCommand(this, "repeat <perk>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length > 0) {
                    LevelPerk perk = VillageManager.getInstance().getPerk(String.join(" ", args));
                    if (perk == null) {
                        getError("unknown-perk").tell(resident, "perk", String.join(" ", args));
                    } else if (!resident.getVillage().hasUnlocked(perk)) {
                        getError("not-unlocked").tell(resident, "perk", perk.getTag());
                    } else if (!perk.matchesLevelRequirement(resident.getVillage())) {
                        getError("level-too-low").tell(resident,
                                "perk", perk.getTag(),
                                "state", perk.getState().getTag(),
                                "level", String.valueOf(perk.getLevel())
                        );
                    } else if (!resident.getVillage().checkPerkCount(perk)) {
                        getError("already-repeated").tell(resident, "perk", perk.getTag(), "amount", String.valueOf(perk.getRepeat()));
                    } else if (resident.getVillage().remove(perk.getRepeatCost())) {
                        resident.getVillage().repeatPerk(resident.getPlayer(), perk);
                        MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("success"), "perk", perk.getTag());
                        DatabaseManager.getInstance().saveSettlement(resident.getVillage());
                        Utils.runAsync(() -> DatabaseManager.getInstance().log(resident.getVillage(), resident, "perk-repeat", perk.getRepeatCost()));
                    } else {
                        getError("not-possible").tell(resident,
                                "perk", perk.getTag(),
                                "reason", getText("missing-items") + "\n"
                                + perk.getUnlockCost().entrySet().stream()
                                .filter(e -> !resident.getVillage().has(e.getKey(), e.getValue()))
                                .map(e -> getMessage("entry").getReplacedText(
                                        "material", e.getKey(),
                                        "amount", String.valueOf(resident.getVillage().getAmount(e.getKey()))))
                                .collect(Collectors.joining("\n"))
                        );
                    }
                    return true;
                }
                getMessage("usage.head").tell(resident, "name", "Repeat Perks");
                for (LevelPerk perk : VillageManager.getInstance().getPerks()) {
                    if (resident.getVillage().hasUnlocked(perk) && resident.getVillage().checkPerkCount(perk)) {
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
