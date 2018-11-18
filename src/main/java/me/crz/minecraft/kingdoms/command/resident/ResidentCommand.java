package me.crz.minecraft.kingdoms.command.resident;

import me.crz.minecraft.kingdoms.chat.commands.IgnoreCommand;
import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import org.bukkit.entity.Player;

public class ResidentCommand extends SubCommand {

    private final SubCommand showCommand;

    public ResidentCommand() {
        super(null, "resident <name>|show <name>");
        setForceOwnUsage(true);
        setHelpCommand(new HelpCommand(this));
        showCommand = new ResidentShowCommand(this);
        registerCommand(showCommand);
        registerCommand(new IgnoreCommand());
        registerCommand(new ResidentClaimTransferCommand(this));
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        if (args.length == 1) {
            String path = base != null && !base.isEmpty() ? (base + " " + alias) : alias;
            return showCommand.execute(p, path, "show", args);
        }
        return false;
    }
}
