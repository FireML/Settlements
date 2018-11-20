package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.ChatCommand;
import me.crz.minecraft.kingdoms.command.PlayerSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.lock.LockCommand;
import me.crz.minecraft.kingdoms.command.lock.UnlockCommand;
import me.crz.minecraft.kingdoms.command.plot.PlotCommand;
import me.crz.minecraft.kingdoms.command.village.money.VillageMoneyCommand;
import me.crz.minecraft.kingdoms.db.LogType;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.teleporter.TeleportersGUI;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VillageCommand extends SubCommand {
    public VillageCommand() {
        super(null, "village", "settlement", "s", "v");
        for (VillageRank rank : VillageRank.values()) {
            getHelpCommand().registerCommand(new VillageHelpCommand(getHelpCommand(), rank));
        }
        getHelpCommand().registerCommand(new VillageHelpCommand(getHelpCommand(), null));
        getHelpCommand().setDefaultTypeProvider(s -> s instanceof Player && ResidentManager.getInstance().doesResidentHaveVillage((Player) s) ? "resident" : "none");
        registerCommand(new ChatCommand());
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
        registerCommand(new LockCommand());
        registerCommand(new UnlockCommand());
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
        registerCommand(new PlayerSubCommand(this, "teleporters") {
            @Override
            public boolean run(Player player, String base, String alias, String... args) {
                TeleportersGUI.getInstance().build(player);
                return true;
            }
        });
        registerCommand(new VillageTitleCommand(this));
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
