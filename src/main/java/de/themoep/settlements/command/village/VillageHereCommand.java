package de.themoep.settlements.command.village;

import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.command.SubCommand;
import org.bukkit.entity.Player;

public class VillageHereCommand extends PlayerSubCommand {

    public VillageHereCommand(SubCommand parent) {
        super(parent, "here");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getAt(player.getLocation());
        if (village == null) {
            getError("no-village").tell(player);
            return true;
        }
        getMessage(village.hasEmpire() ? "empire" : village.isAbandoned() ? "abandoned" : "general")
                .tell(player, village.getReplacements(this));
        return true;
    }
}
