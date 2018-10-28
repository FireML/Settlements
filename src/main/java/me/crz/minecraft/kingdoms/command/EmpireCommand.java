package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.command.empire.EmpireCreateCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireDepositCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireDescriptionCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireDisbandCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireHistoryCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireHomeCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireInviteCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireJoinCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireKickCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireLeaveCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireListCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireMaterialsCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireMemberListCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireModeCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireMoneyCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpirePerksCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireRenameCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireSetHomeCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireSetRankCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireSettingsCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireShowCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireTransferCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireUpgradeCommand;
import me.crz.minecraft.kingdoms.db.LogType;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import org.bukkit.entity.Player;

public class EmpireCommand extends SubCommand {
    public EmpireCommand() {
        super(null, "empire");
        registerCommand(new EmpireCreateCommand(this));
        registerCommand(new EmpireDepositCommand(this));
        registerCommand(new EmpireDescriptionCommand(this));
        registerCommand(new EmpireDisbandCommand(this));
        registerCommand(new EmpireHistoryCommand(this, EmpireRank.LEADER, LogType.ALL));
        registerCommand(new EmpireHomeCommand(this));
        registerCommand(new EmpireInviteCommand(this));
        registerCommand(new EmpireJoinCommand(this));
        registerCommand(new EmpireKickCommand(this));
        registerCommand(new EmpireLeaveCommand(this));
        registerCommand(new EmpireListCommand(this));
        registerCommand(new EmpireMaterialsCommand(this));
        registerCommand(new EmpireMemberListCommand(this));
        registerCommand(new EmpireModeCommand(this));
        registerCommand(new EmpireMoneyCommand(this));
        registerCommand(new EmpireRenameCommand(this));
        registerCommand(new EmpireSetHomeCommand(this));
        registerCommand(new EmpireSetRankCommand(this));
        registerCommand(new EmpireSettingsCommand(this));
        registerCommand(new EmpireShowCommand(this));
        registerCommand(new EmpireTransferCommand(this));
        registerCommand(new EmpirePerksCommand(this));
        registerCommand(new EmpireUpgradeCommand(this));
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        return false;
    }
}
