package de.themoep.settlements.command.empire;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.HelpCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.LogType;
import de.themoep.settlements.land.gui.BankGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Utils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

public class EmpireMoneyCommand extends EmpireSubCommand {

    public EmpireMoneyCommand(SubCommand parent) {
        super(parent, "bank <deposit|withdraw|balance|history> <amount>", "money");
        setHelpCommand(new HelpCommand(this));
        getHelpCommand().setDefaultTypeProvider(s -> s instanceof Player && ResidentManager.getInstance().doesResidentHaveEmpire((Player) s) ? "member" : "none");
        EmpireSubCommand balanceCommand = new EmpireSubCommand(this, "balance") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                getMessage("balance").tell(resident, "amount", Utils.CURRENCY_FORMAT.format(resident.getSettlement().getEmpire().getBank()));
                return true;
            }
        };
        parent.registerCommand(balanceCommand);
        registerCommand(balanceCommand);
        registerCommand(new EmpireSubCommand(this, "deposit <amount>") {
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
                            resident.getSettlement().getEmpire().addToBank(deposit);
                            DatabaseManager.getInstance().saveEmpire(resident.getSettlement().getEmpire());
                            Utils.runAsync(() -> DatabaseManager.getInstance().logDeposit(resident.getSettlement().getEmpire(), resident.getSettlement(), resident, deposit));
                            getMessage("deposited").tell(resident.getSettlement().getEmpire().getOnlineResidents(),
                                    "rank", resident.getEmpireRank().getTag(resident.getSettlement().getEmpire()),
                                    "title", resident.getSettlementTitle(),
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
        });
        registerCommand(new EmpireSubCommand(this, "withdraw <amount>") {
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
                    if (withdrewInit <= resident.getSettlement().getEmpire().getBank()) {
                        EconomyResponse r = SettlementsPlugin.getInstance().getEconomy().depositPlayer(resident.getPlayer(), (double) withdrewInit);
                        if (r.transactionSuccess()) {
                            resident.getSettlement().getEmpire().removeFromBank(withdrewInit);
                            DatabaseManager.getInstance().saveEmpire(resident.getSettlement().getEmpire());
                            Utils.runAsync(() -> DatabaseManager.getInstance().logWithdraw(resident.getSettlement().getEmpire(), resident.getSettlement(), resident, withdrewInit));
                            getMessage("withdrawn").tell(resident.getSettlement().getEmpire().getOnlineResidents(),
                                    "rank", resident.getEmpireRank().getTag(resident.getSettlement().getEmpire()),
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
        });
        registerCommand(new EmpireHistoryCommand(this, LogType.BANK));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length == 0) {
            new BankGUI(resident, resident.getSettlement().getEmpire()).open();
            return true;
        }
        return false;
    }
}
