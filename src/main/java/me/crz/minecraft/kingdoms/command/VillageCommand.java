package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.command.village.VillageAlliesCommand;
import me.crz.minecraft.kingdoms.command.village.VillageAllyCommand;
import me.crz.minecraft.kingdoms.command.village.VillageBanishCommand;
import me.crz.minecraft.kingdoms.command.village.VillageBanishedCommand;
import me.crz.minecraft.kingdoms.command.village.VillageBuildCommand;
import me.crz.minecraft.kingdoms.command.village.VillageClaimCommand;
import me.crz.minecraft.kingdoms.command.village.VillageCreateCommand;
import me.crz.minecraft.kingdoms.command.village.VillageDepositCommand;
import me.crz.minecraft.kingdoms.command.village.VillageDescriptionCommand;
import me.crz.minecraft.kingdoms.command.village.VillageDisbandCommand;
import me.crz.minecraft.kingdoms.command.village.VillageHereCommand;
import me.crz.minecraft.kingdoms.command.village.VillageHistoryCommand;
import me.crz.minecraft.kingdoms.command.village.VillageHomeCommand;
import me.crz.minecraft.kingdoms.command.village.VillageInviteCommand;
import me.crz.minecraft.kingdoms.command.village.VillageJoinCommand;
import me.crz.minecraft.kingdoms.command.village.VillageKickCommand;
import me.crz.minecraft.kingdoms.command.village.VillageLeaveCommand;
import me.crz.minecraft.kingdoms.command.village.VillageListCommand;
import me.crz.minecraft.kingdoms.command.village.VillageMapCommand;
import me.crz.minecraft.kingdoms.command.village.VillageMaterialsCommand;
import me.crz.minecraft.kingdoms.command.village.VillageModeCommand;
import me.crz.minecraft.kingdoms.command.village.VillageOutPostCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.command.village.VillageRenameCommand;
import me.crz.minecraft.kingdoms.command.village.VillageResListCommand;
import me.crz.minecraft.kingdoms.command.village.VillageRulesCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSetAccessCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSetHomeCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSetJoinFeeCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSetRankCommand;
import me.crz.minecraft.kingdoms.command.village.VillageShopCommand;
import me.crz.minecraft.kingdoms.command.village.VillageTaxCommand;
import me.crz.minecraft.kingdoms.command.village.VillageShowCommand;
import me.crz.minecraft.kingdoms.command.village.VillageTitleCommand;
import me.crz.minecraft.kingdoms.command.village.VillageTransferCommand;
import me.crz.minecraft.kingdoms.command.village.VillageUnAllyCommand;
import me.crz.minecraft.kingdoms.command.village.VillageUnbanishCommand;
import me.crz.minecraft.kingdoms.command.village.VillageUnclaimCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePerksCommand;
import me.crz.minecraft.kingdoms.command.village.VillageUpgradeCommand;
import me.crz.minecraft.kingdoms.command.village.VillageUpkeepCommand;
import me.crz.minecraft.kingdoms.command.village.VillageUseCommand;
import me.crz.minecraft.kingdoms.command.village.money.VillageMoneyCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSettingsCommand;
import me.crz.minecraft.kingdoms.db.LogType;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import org.bukkit.entity.Player;

public class VillageCommand extends SubCommand {
    public VillageCommand() {
        super(null, "village");
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
        registerCommand(new VillagePlotCommand(this));
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
