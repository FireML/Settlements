package de.themoep.settlements.command.settlement;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SettlementDonateCommand extends ResidentSubCommand {
    public SettlementDonateCommand(SubCommand parent) {
        super(parent, "donate <settlement> <amount>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 2) {
            return false;
        }
        Player player = resident.getPlayer();
        String name = String.join(" ", Arrays.copyOfRange(args, 0, args.length - 1));
        Settlement settlement = SettlementManager.getInstance().getFromName(name);
        if (settlement == null) {
            getError("settlement-doesnt-exist").tell(player, "name", name);
            return false;
        }
        if (!settlement.hasUnlocked("command.donate") && Config.Citizens.UNLOCK_DEPENDENCY) {
            Message.get("gui.unlockables.locked.npc").tell(resident);
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
                    settlement.addToBank(donate);
                    DatabaseManager.getInstance().saveSettlement(settlement);
                    Utils.runAsync(() -> DatabaseManager.getInstance().log(null, settlement, resident, "donate", "cash", donate));
                    if (!settlement.isMember(resident)) {
                        getMessage("success").tell(resident,
                                "amount", Utils.CURRENCY_FORMAT.format(donate),
                                "state", settlement.getStateTag(),
                                "name", settlement.getName()
                        );
                    }
                    getMessage("donated").tell(settlement.getOnlineResidents(),
                            "name", resident.getName(),
                            "amount", Utils.CURRENCY_FORMAT.format(donate));
                } else {
                    resident.tell("&cAn error occurred, please tell an admin!");
                }
            } else {
                getError("not-enough-money").tell(player, "amount", Utils.CURRENCY_FORMAT.format(donate));
            }
        } catch (NumberFormatException e) {
            getError("not-a-number").tell(player, "input", args[args.length - 1]);
            return false;
        }
        return true;
    }
}
