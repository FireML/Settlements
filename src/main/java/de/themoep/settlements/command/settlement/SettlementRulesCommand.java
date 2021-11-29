package de.themoep.settlements.command.settlement;

import org.bukkit.command.CommandSender;
import de.themoep.settlements.command.SubCommand;

public class SettlementRulesCommand extends SubCommand {
    public SettlementRulesCommand(SubCommand parent) {
        super(parent, "rules");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        getMessage("server-rules").tell(sender);
        return true;
    }
}
