package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import org.bukkit.entity.Player;

public class SettlementHereCommand extends PlayerSubCommand {

    public SettlementHereCommand(SubCommand parent) {
        super(parent, "here");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        Settlement settlement = SettlementManager.getInstance().getAt(player.getLocation());
        if (settlement == null) {
            getError("no-settlement").tell(player);
            return true;
        }
        getMessage(settlement.isAbandoned() ? "abandoned" : "general").tell(player, settlement.getReplacements(this));
        return true;
    }
}
