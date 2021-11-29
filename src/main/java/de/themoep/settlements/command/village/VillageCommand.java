package de.themoep.settlements.command.village;

import de.themoep.settlements.command.village.money.VillageMoneyCommand;
import de.themoep.settlements.command.village.outpost.OutpostTeleportCommand;
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

public class VillageCommand extends SubCommand {
    public VillageCommand() {
        super(null, "settlement", "village", "kingdom", "s", "v", "k");
        setHelpCommand(new HelpCommand(this));
        registerCommand(new ChatCommand());
        registerCommand(new VillageActiveCommand(this));
        registerCommand(new VillageAlliesCommand(this));
        registerCommand(new VillageAllyCommand(this));
        registerCommand(new VillageAllyclaimCommand(this));
        registerCommand(new VillageBanishedCommand(this));
        registerCommand(new VillageBanishCommand(this));
        registerCommand(new VillageUnbanishCommand(this));
        registerCommand(new VillageBuildCommand(this));
        registerCommand(new VillageClaimCommand(this));
        registerCommand(new VillageCreateCommand(this));
        registerCommand(new VillageDepositCommand(this));
        registerCommand(new VillageDescriptionCommand(this));
        registerCommand(new VillageDisbandCommand(this));
        registerCommand(new VillageDiscordCommand(this));
        registerCommand(new VillageDonateCommand(this));
        if (ConfigManager.getInstance().getEventsConf().getConfig().getBoolean("enabled")) {
            registerCommand(new VillageEventsCommand(this));
            registerCommand(new VillageRewardsCommand(this));
        }
        registerCommand(new VillageHereCommand(this));
        registerCommand(new VillageHistoryCommand(this, LogType.ALL));
        registerCommand(new VillageHomeCommand(this));
        registerCommand(new VillageInviteCommand(this));
        registerCommand(new VillageJoinCommand(this));
        registerCommand(new VillageKickCommand(this));
        registerCommand(new VillageLeaveCommand(this));
        registerCommand(new VillageListCommand(this));
        registerCommand(new VillageMailCommand(this));
        registerCommand(new VillageMapCommand(this));
        registerCommand(new VillageMaterialsCommand(this));
        registerCommand(new VillageModeCommand(this));
        registerCommand(new VillageMoneyCommand(this));
        VillageSubCommand outpostCommand = new VillageOutPostCommand(this);
        registerCommand(outpostCommand);
        registerCommand(new OutpostTeleportCommand(outpostCommand));
        registerCommand(new VillagePerksCommand(this));
        registerCommand(new PlotCommand(this));
        if (Config.Lock.ENABLED) {
            registerCommand(new LockCommand());
            registerCommand(new UnlockCommand());
        }
        registerCommand(new VillageRanksCommand(this));
        registerCommand(new VillageRenameCommand(this));
        registerCommand(new VillageResListCommand(this));
        registerCommand(new VillageRulesCommand(this));
        registerCommand(new VillageSetAccessCommand(this));
        registerCommand(new VillageSetHomeCommand(this));
        registerCommand(new VillageJoinFeeCommand(this));
        registerCommand(new VillageSetRankCommand(this));
        registerCommand(new VillageSetTitleCommand(this));
        registerCommand(new VillageSettingsCommand(this));
        registerCommand(new VillageShopCommand(this));
        registerCommand(new VillageShowCommand(this));
        registerCommand(new VillageTaxCommand(this));
        registerCommand(new PlayerSubCommand(this, "teleporters") {
            @Override
            public boolean run(Player player, String base, String alias, String... args) {
                new TeleportersGUI().open(player);
                return true;
            }
        });
        registerCommand(new VillageTitleCommand(this));
        registerCommand(new VillageTitlesCommand(this));
        registerCommand(new VillageTransferCommand(this));
        registerCommand(new VillageUnAllyCommand(this));
        registerCommand(new VillageUnclaimCommand(this));
        registerCommand(new VillageUpgradeCommand(this));
        registerCommand(new VillageUpkeepCommand(this));
        registerCommand(new VillageUseCommand(this));
    }

    @Override
    public boolean run(CommandSender p, String base, String alias, String... args) {
        return false;
    }
}
