package de.themoep.settlements.command.empire;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.settlement.SettlementDepositCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.leveling.LevelInfo;
import de.themoep.settlements.leveling.Levelable;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class EmpireDonateCommand extends ResidentSubCommand {
    public EmpireDonateCommand(SubCommand parent) {
        super(parent, "donate <empire> <amount>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 2) {
            return false;
        }
        Player player = resident.getPlayer();
        String name = String.join(" ", Arrays.copyOfRange(args, 0, args.length - 1));
        Empire empire = EmpireManager.getInstance().getEmpireFromName(name);
        if (empire == null) {
            getError("empire-doesnt-exist").tell(player, "name", name);
            return false;
        }
        if (!empire.hasUnlocked("command.donate")) {
            resident.tell(getUnlockText(empire, "command.donate"));
            return true;
        }
        try {
            long donate = Long.parseLong(args[args.length - 1]);
            if (donate <= 0) {
                return false;
            }
            if (SettlementsPlugin.getInstance().getEconomy().getBalance(player) >= donate) {
                EconomyResponse r2 = SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(player, (double) donate);
                if (r2.transactionSuccess()) {
                    empire.addToBank(donate);
                    Levelable<? extends LevelInfo> levelable = SettlementDepositCommand.getLevellable(empire);
                    if (levelable != null) {
                        levelable.checkLevelUp(resident);
                    }
                    Utils.runAsync(() -> {
                        DatabaseManager.getInstance().saveEmpire(empire);
                        DatabaseManager.getInstance().log(empire, null, resident, "donate", "cash", donate);
                    });
                    if (!empire.isMember(resident)) {
                        getMessage("success").tell(resident,
                                "amount", Utils.CURRENCY_FORMAT.format(donate),
                                "name", empire.getName()
                        );
                    }
                    getMessage("donated").tell(empire.getOnlineResidents(),
                            "name", resident.getName(),
                            "amount", Utils.CURRENCY_FORMAT.format(donate));
                } else {
                    resident.tell("&cAn error occurred, please tell an admin!");
                }
            } else {
                getError("not-enough-money").tell(player, "amount", String.valueOf(donate));
            }
        } catch (NumberFormatException e) {
            getError("not-a-number").tell(player, "input", args[args.length - 1]);
            return false;
        }
        return true;
    }
}
