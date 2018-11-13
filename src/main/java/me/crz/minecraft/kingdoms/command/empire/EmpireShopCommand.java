package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.empire.gui.ShopGUI;
import me.crz.minecraft.kingdoms.perk.EmpirePerk;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.stream.Collectors;

public class EmpireShopCommand extends EmpireSubCommand {
    public EmpireShopCommand(SubCommand parent) {
        super(parent, "shop", EmpireRank.NONE, "buyperks");
        setPermission("kingdoms.user");
        setForceOwnUsage(true);
        registerCommand(new EmpireSubCommand(this, "unlock <perk>", EmpireRank.LEADER, "buy") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length > 0) {
                    EmpirePerk perk = EmpireManager.getInstance().getPerk(String.join(" ", args));
                    if (perk == null) {
                        getError("unknown-perk").tell(resident, "perk", String.join(" ", args));
                    } else if (resident.getVillage().getEmpire().hasUnlocked(perk)) {
                        getError("already-unlocked").tell(resident, "perk", perk.getTag());
                    } else if (resident.getVillage().remove(perk.getUnlockCost())) {
                        resident.getVillage().getEmpire().unlockPerk(perk);
                        MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("success"), "perk", perk.getTag());
                        Utils.runAsync(() -> {
                            DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire());
                            DatabaseManager.getInstance().log(resident.getVillage().getEmpire(), resident, "perk-unlock", perk.getUnlockCost());
                        });
                    } else {
                        getError("not-possible").tell(resident,
                                "perk", perk.getTag(),
                                "reason", getText("missing-items") + "\n"
                                + perk.getUnlockCost().entrySet().stream()
                                .filter(e -> !resident.getVillage().getEmpire().has(e.getKey(), e.getValue()))
                                .map(e -> getMessage("entry").getReplacedText(
                                        "material", e.getKey(),
                                        "amount", String.valueOf(resident.getVillage().getEmpire().getAmount(e.getKey()))))
                                .collect(Collectors.joining("\n"))
                        );
                    }
                    return true;
                }
                getMessage("usage.head").tell(resident, "name", "Unlock Perks");
                for (EmpirePerk perk : EmpireManager.getInstance().getPerks()) {
                    if (!resident.getVillage().getEmpire().hasUnlocked(perk)) {
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
                    } else if (!resident.getVillage().getEmpire().hasUnlocked(perk)) {
                        getError("not-unlocked").tell(resident, "perk", perk.getTag());
                    } else if (!resident.getVillage().getEmpire().checkPerkCount(perk)) {
                        getError("already-repeated").tell(resident, "perk", perk.getTag(), "amount", String.valueOf(perk.getRepeat()));
                    } else if (resident.getVillage().getEmpire().remove(perk.getRepeatCost())) {
                        resident.getVillage().getEmpire().repeatPerk(perk);
                        MessageUtils.tellResidents(resident.getVillage().getEmpire().getOnlineResidents(), getMessage("success"), "perk", perk.getTag());
                        Utils.runAsync(() -> {
                            DatabaseManager.getInstance().saveVillage(resident.getVillage());
                            DatabaseManager.getInstance().log(resident.getVillage().getEmpire(), resident, "perk-repeat", perk.getRepeatCost());
                        });
                    } else {
                        getError("not-possible").tell(resident,
                                "perk", perk.getTag(),
                                "reason", getText("missing-items") + "\n"
                                + perk.getUnlockCost().entrySet().stream()
                                .filter(e -> !resident.getVillage().getEmpire().has(e.getKey(), e.getValue()))
                                .map(e -> getMessage("entry").getReplacedText(
                                        "material", e.getKey(),
                                        "amount", String.valueOf(resident.getVillage().getEmpire().getAmount(e.getKey()))))
                                .collect(Collectors.joining("\n"))
                        );
                    }
                    return true;
                }
                getMessage("usage.head").tell(resident, "name", "Repeat Perks");
                for (EmpirePerk perk : EmpireManager.getInstance().getPerks()) {
                    if (resident.getVillage().getEmpire().hasUnlocked(perk) && resident.getVillage().getEmpire().checkPerkCount(perk)) {
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
