package de.themoep.settlements.commandold.settlement;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.resident.Resident;
import org.bukkit.Bukkit;

public class SettlementCommandsCommand extends SettlementSubCommand {
    public SettlementCommandsCommand(SubCommand parent) {
        super(parent, "commands");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Bukkit.getServer().dispatchCommand(res.getPlayer(), "cp s_command");
        return true;
    }
}
