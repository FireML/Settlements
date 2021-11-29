package de.themoep.settlements.command.village.money;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.village.VillageSubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.utils.Utils;
import net.milkbowl.vault.economy.EconomyResponse;

public class MoneyWithdrawCommand extends VillageSubCommand {
    public MoneyWithdrawCommand(SubCommand parent) {
        super(parent, "withdraw <amount>", "wd");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        try {
            long withdrewInit = Long.parseLong(args[0]);
            if (withdrewInit <= 0) {
                return false;
            }
            if (withdrewInit <= resident.getSettlement().getBank()) {
                EconomyResponse r = SettlementsPlugin.getInstance().getEconomy().depositPlayer(resident.getPlayer(), (double) withdrewInit);
                if (r.transactionSuccess()) {
                    resident.getSettlement().removeFromBank(withdrewInit);
                    DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
                    Utils.runAsync(() -> DatabaseManager.getInstance().logWithdraw(resident.getSettlement(), resident, withdrewInit));
                    getMessage("withdrawn").tell(resident.getSettlement().getOnlineResidents(),
                            "rank", resident.getSettlementRank().getTag(resident.getSettlement()),
                            "title", resident.getSettlementTitle(),
                            "displaytitle", resident.getDisplayTitle(),
                            "name", resident.getName(),
                            "amount", Utils.CURRENCY_FORMAT.format(withdrewInit));
                } else {
                    resident.tell("&cAn error occurred, please tell an admin!");
                }
            } else {
                getError("not-enough-money").tell(resident, "amount", Utils.CURRENCY_FORMAT.format(withdrewInit));
            }
        } catch (NumberFormatException e) {
            getError("not-a-number").tell(resident, "input", args[0]);
            return false;
        }
        return true;
    }
}
