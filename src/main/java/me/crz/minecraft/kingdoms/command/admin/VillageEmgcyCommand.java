package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.Base;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageEmgcyCommand extends SubCommand {
    public VillageEmgcyCommand(SubCommand parent) {
        super(parent, "emgcy");
        setPermission("kingdoms.admin");
        setVisible(false);
    }

    @Override
    public boolean run(final Player player, final String base, final String alias, final String... args) {
        Base.getPlugin().emgcy = !Base.getPlugin().emgcy;
        return true;
    }
}
