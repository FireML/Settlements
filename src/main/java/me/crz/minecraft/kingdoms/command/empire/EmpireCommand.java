package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.ChatCommand;
import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.lock.LockCommand;
import me.crz.minecraft.kingdoms.command.lock.UnlockCommand;
import me.crz.minecraft.kingdoms.command.plot.PlotCommand;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EmpireCommand extends SubCommand {
    public EmpireCommand() {
        super(null, "empire", "e");
        setHelpCommand(new HelpCommand(this));
        for (VillageRank rank : VillageRank.values()) {
            if (rank != VillageRank.getDefault()) {
                getHelpCommand().registerCommand(new EmpireHelpCommand(getHelpCommand(), rank));
            }
        }
        for (EmpireRank rank : EmpireRank.values()) {
            if (rank != EmpireRank.getDefault()) {
                getHelpCommand().registerCommand(new EmpireHelpCommand(getHelpCommand(), rank));
            }
        }
        getHelpCommand().setDefaultTypeProvider(s -> s instanceof Player && ResidentManager.getInstance().doesResidentHaveEmpire((Player) s) ? "member" : "no");
        registerCommand(new ChatCommand());
        registerCommand(new EmpireAlliesCommand(this));
        registerCommand(new EmpireAllyCommand(this));
        registerCommand(new EmpireBanishCommand(this));
        registerCommand(new EmpireBanishedCommand(this));
        registerCommand(new EmpireBuildCommand(this));
        registerCommand(new EmpireCreateCommand(this));
        registerCommand(new EmpireDescriptionCommand(this));
        registerCommand(new EmpireDisbandCommand(this));
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
        registerCommand(new EmpireRegenCommand(this));
        registerCommand(new EmpirePerksCommand(this));
        registerCommand(new EmpirePermsCommand(this));
        registerCommand(new PlotCommand());
        registerCommand(new LockCommand());
        registerCommand(new UnlockCommand());
        registerCommand(new EmpireSenatorsCommand(this));
        registerCommand(new EmpireSetAccessCommand(this));
        registerCommand(new EmpireSetHomeCommand(this));
        registerCommand(new EmpireJoinFeeCommand(this));
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
    public boolean run(CommandSender s, String base, String alias, String... args) {
        return false;
    }
}
