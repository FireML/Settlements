package de.themoep.settlements.commandold.settlement.money;

import de.themoep.settlements.commandold.HelpCommand;
import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.commandold.settlement.SettlementHistoryCommand;
import de.themoep.settlements.commandold.settlement.SettlementSubCommand;
import de.themoep.settlements.db.LogType;
import de.themoep.settlements.land.gui.BankGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;

public class SettlementMoneyCommand extends SettlementSubCommand {

    public SettlementMoneyCommand(SubCommand parent) {
        super(parent, "bank <deposit|withdraw|balance|history> <amount>", "money");
        setHelpCommand(new HelpCommand(this));
        getHelpCommand().setDefaultTypeProvider(s -> s instanceof Player && ResidentManager.getInstance().doesResidentHaveSettlement((Player) s) ? "resident" : "none");
        SettlementSubCommand balanceCommand = new SettlementSubCommand(this, "balance") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                getMessage("balance").tell(resident, "amount", Utils.CURRENCY_FORMAT.format(resident.getSettlement().getBank()));
                return true;
            }
        };
        parent.registerCommand(balanceCommand);
        registerCommand(balanceCommand);
        registerCommand(new MoneyWithdrawCommand(this));
        registerCommand(new MoneyDepositCommand(this));
        registerCommand(new SettlementHistoryCommand(this, LogType.BANK));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length == 0) {
            if (resident.getSettlement().hasUnlocked("command.bank")) {
                new BankGUI(resident, resident.getSettlement()).open();
            } else {
                resident.tell(getUnlockText(resident.getSettlement(), "command.bank"));
            }
            return true;
        }
        return false;
    }
}
