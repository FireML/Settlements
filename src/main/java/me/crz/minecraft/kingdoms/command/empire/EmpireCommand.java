package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.ChatCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.lock.LockCommand;
import me.crz.minecraft.kingdoms.command.lock.UnlockCommand;
import me.crz.minecraft.kingdoms.command.plot.PlotCommand;
import me.crz.minecraft.kingdoms.db.LogType;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import org.bukkit.entity.Player;

public class EmpireCommand extends SubCommand {
    public EmpireCommand() {
        super(null, "empire");
        registerCommand(new ChatCommand());
        registerCommand(new EmpireAlliesCommand(this));
        registerCommand(new EmpireAllyCommand(this));
        registerCommand(new EmpireBanishCommand(this));
        registerCommand(new EmpireBanishedCommand(this));
        registerCommand(new EmpireBuildCommand(this));
        registerCommand(new EmpireCreateCommand(this));
        registerCommand(new EmpireDescriptionCommand(this));
        registerCommand(new EmpireDisbandCommand(this));
        registerCommand(new EmpireHistoryCommand(this, EmpireRank.LEADER, LogType.ALL));
        registerCommand(new EmpireHereCommand(this));
        registerCommand(new EmpireHomeCommand(this));
        registerCommand(new EmpireInviteCommand(this));
        registerCommand(new EmpireJoinCommand(this));
        registerCommand(new EmpireKickCommand(this));
        registerCommand(new EmpireLeaveCommand(this));
        registerCommand(new EmpireListCommand(this));
        registerCommand(new EmpireMapCommand(this));
        registerCommand(new EmpireMemberListCommand(this));
        registerCommand(new EmpireModeCommand(this));
        registerCommand(new EmpireMoneyCommand(this));
        registerCommand(new EmpireRenameCommand(this));
        registerCommand(new EmpirePerksCommand(this));
        registerCommand(new PlotCommand());
        registerCommand(new LockCommand());
        registerCommand(new UnlockCommand());
        registerCommand(new EmpireSetAccessCommand(this));
        registerCommand(new EmpireSetHomeCommand(this));
        registerCommand(new EmpireSetJoinFeeCommand(this));
        registerCommand(new EmpireSetSymbolCommand(this));
        registerCommand(new EmpireSetRankCommand(this));
        registerCommand(new EmpireSettingsCommand(this));
        registerCommand(new EmpireShopCommand(this));
        registerCommand(new EmpireShowCommand(this));
        registerCommand(new EmpireTaxCommand(this));
        registerCommand(new EmpireTransferCommand(this));
        registerCommand(new EmpireUnAllyCommand(this));
        registerCommand(new EmpireUnbanishCommand(this));
        registerCommand(new EmpireUpkeepCommand(this));
        registerCommand(new EmpireUseCommand(this));
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        return false;
    }
}
