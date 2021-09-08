package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import org.bukkit.entity.Player;

public class EmpireHereCommand extends PlayerSubCommand {

    public EmpireHereCommand(SubCommand parent) {
        super(parent, "here");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        Empire empire = EmpireManager.getInstance().getEmpireFromWorld(player.getWorld());
        if (empire == null) {
            getError("not-in-empire").tell(player);
            return true;
        }
        getMessage(empire.isAbandoned() ? "abandoned" : "general").tell(player, empire.getReplacements(this));
        return true;
    }
}
