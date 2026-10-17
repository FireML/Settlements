package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.HelpCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.plot.PlotCommand;
import de.themoep.settlements.command.settlement.money.SettlementMoneyCommand;
import de.themoep.settlements.command.settlement.outpost.OutpostTeleportCommand;
import de.themoep.settlements.db.LogType;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.resident.gui.OverviewGUI;
import de.themoep.settlements.resident.gui.StartGUI;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.firedev.chatchannels.commands.ChatCommand;

public class SettlementCommand extends SubCommand {
    public SettlementCommand() {
        super(null, "settlement", "village", "kingdom", "s", "v", "k");
        setHelpCommand(new HelpCommand(this));
        registerCommand(new SettlementActiveCommand(this));
        registerCommand(new SettlementAlliesCommand(this));
        registerCommand(new SettlementAllyCommand(this));
        registerCommand(new SettlementAllyclaimCommand(this));
        registerCommand(new SettlementAuctionTaxCommand(this));
        registerCommand(new SettlementBanishedCommand(this));
        registerCommand(new SettlementBanishCommand(this));
        registerCommand(new SettlementUnbanishCommand(this));
        registerCommand(new SettlementBuildCommand(this));
        registerCommand(new SettlementClaimCommand(this));
        registerCommand(new SettlementCreateCommand(this));
        registerCommand(new SettlementDescriptionCommand(this));
        registerCommand(new SettlementDisbandCommand(this));
        registerCommand(new SettlementDiscordCommand(this));
        registerCommand(new SettlementDonateCommand(this));
        if (ConfigManager.getInstance().getEventsConf().getConfig().getBoolean("enabled")) {
            registerCommand(new SettlementEventsCommand(this));
            registerCommand(new SettlementRewardsCommand(this));
        }
        registerCommand(new SettlementHereCommand(this));
        registerCommand(new SettlementHistoryCommand(this, LogType.ALL));
        registerCommand(new SettlementHomeCommand(this));
        registerCommand(new SettlementInviteCommand(this));
        registerCommand(new SettlementJoinCommand(this));
        registerCommand(new SettlementKickCommand(this));
        registerCommand(new SettlementKickNoticeCommand(this));
        registerCommand(new SettlementLeaveCommand(this));
        registerCommand(new SettlementListCommand(this));
        registerCommand(new SettlementMapCommand(this));
        if (Config.Settlement.Missions.ENABLED) {
            registerCommand(new SettlementMissionsCommand(this));
        }
        registerCommand(new SettlementModeCommand(this));
        registerCommand(new SettlementMoneyCommand(this));
        SettlementSubCommand outpostCommand = new SettlementOutPostCommand(this);
        registerCommand(outpostCommand);
        registerCommand(new OutpostTeleportCommand(outpostCommand));
        registerCommand(new SettlementPerksCommand(this));
        registerCommand(new PlotCommand());
        registerCommand(new SettlementRanksCommand(this));
        registerCommand(new SettlementRenameCommand(this));
        registerCommand(new SettlementResCommand(this));
        registerCommand(new SettlementResListCommand(this));
        registerCommand(new SettlementRulesCommand(this));
        registerCommand(new SettlementSetAccessCommand(this));
        registerCommand(new SettlementSetRecruitmentCommand(this));
        registerCommand(new SettlementSetHomeCommand(this));
        registerCommand(new SettlementJoinFeeCommand(this));
        registerCommand(new SettlementSetRankCommand(this));
        registerCommand(new SettlementSetTitleCommand(this));
        registerCommand(new SettlementSettingsCommand(this));
        registerCommand(new SettlementShopCommand(this));
        registerCommand(new SettlementShowCommand(this));
        registerCommand(new SettlementTaxCommand(this));
        registerCommand(new SettlementTitleCommand(this));
        registerCommand(new SettlementTitlesCommand(this));
        registerCommand(new SettlementTransferCommand(this));
        registerCommand(new SettlementUnAllyCommand(this));
        registerCommand(new SettlementUnclaimCommand(this));
        registerCommand(new SettlementDepositCommand(this));
        registerCommand(new SettlementUpkeepCommand(this));
        registerCommand(new SettlementUpgradeCommand(this));
        registerCommand(new SettlementUseCommand(this));
        registerCommand(new SettlementCommandsCommand(this));
    }

    @Override
    public boolean run(CommandSender p, String base, String alias, String... args) {
        if (p instanceof Player player) {
            Resident resident = ResidentManager.getInstance().getResident(player);
            if (resident.hasSettlement()) {
                new OverviewGUI(resident).open();
            } else {
                new StartGUI(resident).open();
            }
            return true;
        }
        return false;
    }
}
