package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.settlement.money.SettlementMoneyCommand;
import de.themoep.settlements.command.settlement.outpost.OutpostTeleportCommand;
import de.themoep.settlements.teleporter.TeleportersGUI;
import de.themoep.settlements.command.ChatCommand;
import de.themoep.settlements.command.HelpCommand;
import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.lock.LockCommand;
import de.themoep.settlements.command.lock.UnlockCommand;
import de.themoep.settlements.command.plot.PlotCommand;
import de.themoep.settlements.db.LogType;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettlementCommand extends SubCommand {
    public SettlementCommand() {
        super(null, "settlement", "village", "kingdom", "s", "v", "k");
        setHelpCommand(new HelpCommand(this));
        registerCommand(new ChatCommand());
        registerCommand(new SettlementActiveCommand(this));
        registerCommand(new SettlementAlliesCommand(this));
        registerCommand(new SettlementAllyCommand(this));
        registerCommand(new SettlementAllyclaimCommand(this));
        registerCommand(new SettlementBanishedCommand(this));
        registerCommand(new SettlementBanishCommand(this));
        registerCommand(new SettlementUnbanishCommand(this));
        registerCommand(new SettlementBuildCommand(this));
        registerCommand(new SettlementClaimCommand(this));
        registerCommand(new SettlementCreateCommand(this));
        registerCommand(new SettlementDepositCommand(this));
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
        registerCommand(new SettlementLeaveCommand(this));
        registerCommand(new SettlementListCommand(this));
        registerCommand(new SettlementMailCommand(this));
        registerCommand(new SettlementMapCommand(this));
        registerCommand(new SettlementMaterialsCommand(this));
        registerCommand(new SettlementModeCommand(this));
        registerCommand(new SettlementMoneyCommand(this));
        SettlementSubCommand outpostCommand = new SettlementOutPostCommand(this);
        registerCommand(outpostCommand);
        registerCommand(new OutpostTeleportCommand(outpostCommand));
        registerCommand(new SettlementPerksCommand(this));
        registerCommand(new PlotCommand(this));
        if (Config.Lock.ENABLED) {
            registerCommand(new LockCommand());
            registerCommand(new UnlockCommand());
        }
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
        registerCommand(new PlayerSubCommand(this, "teleporters") {
            @Override
            public boolean run(Player player, String base, String alias, String... args) {
                new TeleportersGUI().open(player);
                return true;
            }
        });
        registerCommand(new SettlementTitleCommand(this));
        registerCommand(new SettlementTitlesCommand(this));
        registerCommand(new SettlementTransferCommand(this));
        registerCommand(new SettlementUnAllyCommand(this));
        registerCommand(new SettlementUnclaimCommand(this));
        registerCommand(new SettlementUpgradeCommand(this));
        registerCommand(new SettlementUpkeepCommand(this));
        registerCommand(new SettlementUseCommand(this));
    }

    @Override
    public boolean run(CommandSender p, String base, String alias, String... args) {
        return false;
    }
}
