package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.ChatCommand;
import de.themoep.settlements.command.HelpCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.lock.LockCommand;
import de.themoep.settlements.command.lock.UnlockCommand;
import de.themoep.settlements.command.plot.PlotCommand;
import de.themoep.settlements.utils.config.Config;
import org.bukkit.command.CommandSender;

public class EmpireCommand extends SubCommand {
    public EmpireCommand() {
        super(null, "empire", "e");
        setHelpCommand(new HelpCommand(this));
        registerCommand(new ChatCommand());
        registerCommand(new EmpireAlliesCommand(this));
        registerCommand(new EmpireAllyCommand(this));
        registerCommand(new EmpireAuctionTaxCommand(this));
        registerCommand(new EmpireBanishCommand(this));
        registerCommand(new EmpireBanishedCommand(this));
        registerCommand(new EmpireBuildCommand(this));
        registerCommand(new EmpireCreateCommand(this));
        registerCommand(new EmpireDescriptionCommand(this));
        registerCommand(new EmpireDisbandCommand(this));
        registerCommand(new EmpireDiscordCommand(this));
        registerCommand(new EmpireDonateCommand(this));
        registerCommand(new EmpireHereCommand(this));
        registerCommand(new EmpireHomeCommand(this));
        registerCommand(new EmpireInviteCommand(this));
        registerCommand(new EmpireJoinCommand(this));
        registerCommand(new EmpireKickCommand(this));
        registerCommand(new EmpireLeaveCommand(this));
        registerCommand(new EmpireListCommand(this));
        registerCommand(new EmpireMailCommand(this));
        registerCommand(new EmpireMapCommand(this));
        registerCommand(new EmpireMemberListCommand(this));
        if (Config.Empire.Missions.ENABLED) {
            registerCommand(new EmpireMissionsCommand(this));
        }
        registerCommand(new EmpireModeCommand(this));
        registerCommand(new EmpireMoneyCommand(this));
        registerCommand(new EmpireRenameCommand(this));
        registerCommand(new EmpirePerksCommand(this));
        registerCommand(new EmpireRanksCommand(this));
        registerCommand(new PlotCommand(this));
        if (Config.Lock.ENABLED) {
            registerCommand(new LockCommand());
            registerCommand(new UnlockCommand());
        }
        registerCommand(new EmpireSetAccessCommand(this));
        registerCommand(new EmpireSetRecruitmentCommand(this));
        registerCommand(new EmpireSetHomeCommand(this));
        registerCommand(new EmpireJoinFeeCommand(this));
        registerCommand(new EmpireSetSymbolCommand(this));
        registerCommand(new EmpireSetRankCommand(this));
        registerCommand(new EmpireSettingsCommand(this));
        registerCommand(new EmpireShopCommand(this));
        registerCommand(new EmpireShowCommand(this));
        registerCommand(new EmpireTaxCommand(this));
        registerCommand(new EmpireTitleCommand(this));
        registerCommand(new EmpireTitlesCommand(this));
        registerCommand(new EmpireTransferCommand(this));
        registerCommand(new EmpireUnAllyCommand(this));
        registerCommand(new EmpireUnbanishCommand(this));
        registerCommand(new EmpireUpgradeCommand(this));
        registerCommand(new EmpireUpkeepCommand(this));
        registerCommand(new EmpireUseCommand(this));
        if (Config.Empire.World.ENABLED) {
            registerCommand(new EmpireWorldCommand(this));
            registerCommand(new EmpireRegenCommand(this));
        }
    }

    @Override
    public boolean run(CommandSender s, String base, String alias, String... args) {
        return false;
    }
}
