package de.themoep.settlements.command.village;

import de.themoep.settlements.village.Settlement;
import de.themoep.settlements.village.SettlementManager;
import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.command.SubCommand;
import org.bukkit.entity.Player;

public class VillageHereCommand extends PlayerSubCommand {

    public VillageHereCommand(SubCommand parent) {
        super(parent, "here");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        Settlement settlement = SettlementManager.getInstance().getAt(player.getLocation());
        if (settlement == null) {
            getError("no-settlement").tell(player);
            return true;
        }
        getMessage(settlement.hasEmpire() ? "empire" : settlement.isAbandoned() ? "abandoned" : "general")
                .tell(player, settlement.getReplacements(this));
        return true;
    }
}
