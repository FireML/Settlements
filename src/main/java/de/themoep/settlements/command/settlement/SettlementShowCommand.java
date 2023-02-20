package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettlementShowCommand extends SubCommand {

    public SettlementShowCommand(SubCommand parent) {
        super(parent, "show [<name>]", "info");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length == 0 && sender instanceof Player) {
            Settlement settlement = SettlementManager.getInstance().getFromPlayer((Player) sender);
            if (settlement == null) {
                MessageUtils.tell(sender, getError("not-in-settlement"));
                return false;
            }
            getMessage(settlement.hasEmpire() ? "empire" : settlement.isAbandoned() ? "abandoned" : "general")
                    .tell(sender, settlement.getReplacements(this));
        } else if (args.length == 1) {
            String name = String.join(" ", args);
            Settlement settlement = SettlementManager.getInstance().getFromName(name);
            if (settlement != null) {
                getMessage(settlement.hasEmpire() ? "empire" : settlement.isAbandoned() ? "abandoned" : "general")
                        .tell(sender, settlement.getReplacements(this));
                return true;
            }
            Utils.runAsync(() -> {
                Resident target = ResidentManager.getInstance().getResident(name);
                if (target != null && target.hasSettlement()) {
                    getMessage(target.hasEmpire() ? "empire" : target.getSettlement().isAbandoned() ? "abandoned" : "general")
                            .tell(sender, target.getSettlement().getReplacements(this));
                } else {
                    getError("settlement-doesnt-exist").tell(sender, "name", name);
                }
            });
        } else {
            return false;
        }
        return true;
    }
}
