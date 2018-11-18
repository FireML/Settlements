package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import org.bukkit.entity.Player;

public class VillageAdminCommand extends SubCommand {

    public VillageAdminCommand() {
        super(null, "villageadmin", "va");
        setPermission("kingdoms.admin");
        setHelpCommand(new HelpCommand(this));
        this.registerCommand(new VillageAdminBonusCommand(this));
        this.registerCommand(new VillageAdminWorldCommand(this));
        this.registerCommand(new VillageAdminRankCommand(this));
        this.registerCommand(new VillageAdminJoinCommand(this));
        this.registerCommand(new VillageAdminLevelCommand(this));
        this.registerCommand(new VillageAdminReloadCommand(this));
        this.registerCommand(new VillageEmgcyCommand(this));
        this.registerCommand(new VillageAdminSocialSpyCommand(this));
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        return false;
    }

    public int ordinal() {
        return Integer.MAX_VALUE;
    }
}
