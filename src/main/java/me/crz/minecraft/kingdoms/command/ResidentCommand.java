package me.crz.minecraft.kingdoms.command;

import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.resident.ResidentFriendCommand;
import me.crz.minecraft.kingdoms.command.resident.ResidentShowCommand;

public class ResidentCommand extends SubCommand {

    public ResidentCommand() {
        super(null, "resident");
        registerCommand(new ResidentShowCommand(this));
        registerCommand(new ResidentFriendCommand(this));
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        return false;
    }
}
