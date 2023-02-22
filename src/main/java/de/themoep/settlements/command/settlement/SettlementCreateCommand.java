package de.themoep.settlements.command.settlement;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.SettlementState;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import net.milkbowl.vault.economy.EconomyResponse;

import java.util.regex.Pattern;

public class SettlementCreateCommand extends ResidentSubCommand {
    private Pattern allowed = Pattern.compile(Config.Settlement.NAME_VALIDATION);

    public SettlementCreateCommand(SubCommand parent) {
        super(parent, "create <name>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        String name = Utils.filterAllCurses(String.join(" ", args).trim());
        if (name.toLowerCase().contains("claims")) {
            getError("invalid-characters").tell(resident, "input", name);
        } else if (settlementExists(name)) {
            getError("settlement-exists").tell(resident, "name", name);
        } else if (name.length() > Config.Settlement.NAME_LENGTH) {
            getError("too-long").tell(resident, "input", name, "amount", String.valueOf(Config.Settlement.NAME_LENGTH));
        } else if (!allowed.matcher(name).matches()) {
            getError("invalid-characters").tell(resident, "input", name);
        } else {
            if (Config.Settlement.CREATION_COST != 0) {
                if (!SettlementsPlugin.getInstance().getEconomy().has(resident.getPlayer(), Config.Settlement.CREATION_COST)) {
                    getError("not-enough-money").tell(resident, "amount", String.valueOf(Config.Settlement.CREATION_COST));
                    return true;
                }
                EconomyResponse r = SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(resident.getPlayer(), (double) Config.Settlement.CREATION_COST);
                if (!r.transactionSuccess()) {
                    MessageUtils.tell(resident, "&cAn error occurred, please tell an admin!");
                    return true;
                }
            }
            Settlement settlement = new Settlement(name, resident.getUUID(), 1, SettlementState.VILLAGE);
            SettlementManager.getInstance().registerNew(resident, settlement);

            getMessage("broadcast").broadcast(
                    "name", settlement.getName(),
                    "state", settlement.getState().getTag(),
                    "player", resident.getName());
            getMessage("success").tell(resident,
                    "name", settlement.getName(),
                    "cost", Utils.CURRENCY_FORMAT.format(Config.Settlement.CREATION_COST)
            );
        }
        return true;
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (resident.hasSettlement()) {
            getError("free-required").tell(resident);
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return !resident.hasSettlement() && super.hasAccess(resident);
    }


    private boolean settlementExists(String name) {
        return SettlementManager.getInstance().getFromName(name) != null;
    }
}
