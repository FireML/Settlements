package de.themoep.settlements.command.village.money;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.command.HelpCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.village.VillageHistoryCommand;
import de.themoep.settlements.command.village.VillageSubCommand;
import de.themoep.settlements.db.LogType;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;

public class VillageMoneyCommand extends VillageSubCommand {

    public VillageMoneyCommand(SubCommand parent) {
        super(parent, "bank <deposit|withdraw|balance|history> <amount>", "money");
        setHelpCommand(new HelpCommand(this));
        getHelpCommand().setDefaultTypeProvider(s -> s instanceof Player && ResidentManager.getInstance().doesResidentHaveVillage((Player) s) ? "resident" : "none");
        VillageSubCommand balanceCommand = new VillageSubCommand(this, "balance") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                getMessage("balance").tell(resident, "amount", Utils.CURRENCY_FORMAT.format(resident.getVillage().getBank()));
                return true;
            }
        };
        parent.registerCommand(balanceCommand);
        registerCommand(balanceCommand);
        registerCommand(new MoneyWithdrawCommand(this));
        registerCommand(new MoneyDepositCommand(this));
        registerCommand(new VillageHistoryCommand(this, LogType.BANK));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return false;
    }
}
