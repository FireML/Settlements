package me.crz.minecraft.kingdoms.command.village;

import org.bukkit.command.CommandSender;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageRulesCommand extends SubCommand {
    public VillageRulesCommand(SubCommand parent) {
        super(parent, "rules");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        getMessage("server-rules").tell(sender);
        return true;
    }
}
