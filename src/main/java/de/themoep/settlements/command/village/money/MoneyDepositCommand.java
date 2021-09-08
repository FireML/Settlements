package de.themoep.settlements.command.village.money;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.village.VillageSubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.utils.Utils;
import net.milkbowl.vault.economy.EconomyResponse;

public class MoneyDepositCommand extends VillageSubCommand {
    public MoneyDepositCommand(SubCommand parent) {
        super(parent, "deposit <amount>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        try {
            long deposit = Long.parseLong(args[0]);
            if (deposit <= 0) {
                return false;
            }
            if (SettlementsPlugin.getInstance().getEconomy().getBalance(resident.getPlayer()) >= deposit) {
                EconomyResponse r2 = SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(resident.getPlayer(), (double) deposit);
                if (r2.transactionSuccess()) {
                    resident.getVillage().addToBank(deposit);
                    DatabaseManager.getInstance().saveSettlement(resident.getVillage());
                    Utils.runAsync(() -> DatabaseManager.getInstance().logDeposit(resident.getVillage(), resident, deposit));
                    getMessage("deposited").tell(resident.getVillage().getOnlineResidents(),
                            "rank", resident.getVillageRank().getTag(resident.getVillage()),
                            "title", resident.getVillageTitle(),
                            "displaytitle", resident.getDisplayTitle(),
                            "name", resident.getName(),
                            "amount", Utils.CURRENCY_FORMAT.format(deposit));
                } else {
                    resident.tell("&cAn error occurred, please tell an admin!");
                }
            } else {
                getError("not-enough-money").tell(resident, "amount", Utils.CURRENCY_FORMAT.format(deposit));
            }
        } catch (NumberFormatException e) {
            getError("not-a-number").tell(resident, "input", args[0]);
            return false;
        }
        return true;
    }
}
