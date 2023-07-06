package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.UtilTime;
import de.themoep.settlements.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.concurrent.TimeUnit;

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
