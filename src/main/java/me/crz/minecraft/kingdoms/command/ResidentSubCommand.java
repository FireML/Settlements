package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public abstract class ResidentSubCommand extends PlayerSubCommand {

    public ResidentSubCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        Resident resident = ResidentManager.getInstance().getResident(player);
        if (resident != null) {
            return run(resident, base, alias, args);
        }
        player.sendMessage(ChatColor.RED + "ERROR: Plugin was not able to find your Resident info?");
        return true;
    }

    public abstract boolean run(Resident resident, String base, String alias, String... args);

    public int ordinal() {
        return -1;
    }
}
