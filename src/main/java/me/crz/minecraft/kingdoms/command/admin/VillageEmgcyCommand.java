package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.Base;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageEmgcyCommand extends SubCommand
{
    public VillageEmgcyCommand() {
        super("dont touch", new String[] { "emgcy" });
        this.setPermission("village.admin");
        this.setUsage("");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        Base.getPlugin().emgcy = !Base.getPlugin().emgcy;
    }
}
