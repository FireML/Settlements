package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.command.admin.VillageAdminReloadCommand;
import org.bukkit.entity.Player;

import me.crz.minecraft.kingdoms.command.admin.VillageAdminSocialSpyCommand;
import me.crz.minecraft.kingdoms.command.admin.VillageEmgcyCommand;
import me.crz.minecraft.kingdoms.command.admin.VillageAdminJoinCommand;
import me.crz.minecraft.kingdoms.command.admin.VillageAdminRankCommand;
import me.crz.minecraft.kingdoms.command.admin.VillageAdminWorldCommand;

public class VillageAdminCommand extends SubCommand {
    private StringBuilder help;

    public VillageAdminCommand() {
        super(null, "villageadmin", "va");
        setPermission("kingdoms.admin");
        this.registerCommand(new VillageAdminWorldCommand(this));
        this.registerCommand(new VillageAdminRankCommand(this));
        this.registerCommand(new VillageAdminJoinCommand(this));
        this.registerCommand(new VillageAdminReloadCommand(this));
        this.registerCommand(new VillageEmgcyCommand(this));
        this.registerCommand(new VillageAdminSocialSpyCommand(this));
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        return false;
    }
}
