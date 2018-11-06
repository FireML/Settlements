package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.ChatCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.plot.PlotCommand;
import me.crz.minecraft.kingdoms.command.village.money.VillageMoneyCommand;
import me.crz.minecraft.kingdoms.db.LogType;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import org.bukkit.entity.Player;

public class VillageCommand extends SubCommand {
    public VillageCommand() {
        super(null, "village");
        setPermission("kingdoms.user");
        registerCommand(new ChatCommand());
        registerCommand(new VillageAlliesCommand(this));
        registerCommand(new VillageAllyCommand(this));
        registerCommand(new VillageBanishedCommand(this));
        registerCommand(new VillageBanishCommand(this));
        registerCommand(new VillageUnbanishCommand(this));
        registerCommand(new VillageBuildCommand(this));
        registerCommand(new VillageClaimCommand(this));
        registerCommand(new VillageCreateCommand(this));
        registerCommand(new VillageDepositCommand(this));
        registerCommand(new VillageDescriptionCommand(this));
        registerCommand(new VillageDisbandCommand(this));
        registerCommand(new VillageHereCommand(this));
        registerCommand(new VillageHistoryCommand(this, VillageRank.CHIEF, LogType.ALL));
        registerCommand(new VillageHomeCommand(this));
        registerCommand(new VillageInviteCommand(this));
        registerCommand(new VillageJoinCommand(this));
        registerCommand(new VillageKickCommand(this));
        registerCommand(new VillageLeaveCommand(this));
        registerCommand(new VillageListCommand(this));
        registerCommand(new VillageMapCommand(this));
        registerCommand(new VillageMaterialsCommand(this));
        registerCommand(new VillageModeCommand(this));
        registerCommand(new VillageMoneyCommand(this));
        registerCommand(new VillageOutPostCommand(this));
        registerCommand(new VillagePerksCommand(this));
        registerCommand(new PlotCommand());
        registerCommand(new VillageRenameCommand(this));
        registerCommand(new VillageResListCommand(this));
        registerCommand(new VillageRulesCommand(this));
        registerCommand(new VillageSetAccessCommand(this));
        registerCommand(new VillageSetHomeCommand(this));
        registerCommand(new VillageSetJoinFeeCommand(this));
        registerCommand(new VillageSetRankCommand(this));
        registerCommand(new VillageSettingsCommand(this));
        registerCommand(new VillageShopCommand(this));
        registerCommand(new VillageShowCommand(this));
        registerCommand(new VillageTaxCommand(this));
        registerCommand(new VillageTitleCommand(this));
        registerCommand(new VillageTransferCommand(this));
        registerCommand(new VillageUnAllyCommand(this));
        registerCommand(new VillageUnclaimCommand(this));
        registerCommand(new VillageUpgradeCommand(this));
        registerCommand(new VillageUpkeepCommand(this));
        registerCommand(new VillageUseCommand(this));
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        return false;
    }
}
