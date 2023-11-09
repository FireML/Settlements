package de.themoep.settlements.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uk.firedev.poleislib.utils.ComponentUtils;

public class TownyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player p) {
            p.sendMessage(ComponentUtils.parseComponent("&bWe are using a Custom Towny Plugin. Please use &6/s &binstead."));
        }

        return false;
    }
}
