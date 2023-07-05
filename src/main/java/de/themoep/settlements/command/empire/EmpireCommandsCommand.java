package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.UtilTime;
import de.themoep.settlements.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.concurrent.TimeUnit;

public class EmpireCommandsCommand extends EmpireSubCommand {
    public EmpireCommandsCommand(SubCommand parent) {
        super(parent, "commands");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Bukkit.getServer().dispatchCommand(res.getPlayer(), "cp s_command");
        return true;
    }
}
