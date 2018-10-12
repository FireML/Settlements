package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.command.village.VillageSetAccessCommand;
import me.crz.minecraft.kingdoms.utils.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.Arrays;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

import me.crz.minecraft.kingdoms.command.village.VillageBanishCommand;
import me.crz.minecraft.kingdoms.command.village.VillageUpkeepCommand;
import me.crz.minecraft.kingdoms.command.village.settings.VillageSettingsCommand;
import me.crz.minecraft.kingdoms.command.village.buildNuse.VillageUseCommand;
import me.crz.minecraft.kingdoms.command.village.VillageAlliesCommand;
import me.crz.minecraft.kingdoms.command.village.VillageUnAllyCommand;
import me.crz.minecraft.kingdoms.command.village.VillageAllyCommand;
import me.crz.minecraft.kingdoms.command.village.VillageResListCommand;
import me.crz.minecraft.kingdoms.command.village.VillageLockCommand;
import me.crz.minecraft.kingdoms.command.village.VillageRulesCommand;
import me.crz.minecraft.kingdoms.command.village.VillageOutPostCommand;
import me.crz.minecraft.kingdoms.command.village.VillageRenameCommand;
import me.crz.minecraft.kingdoms.command.village.VillageTransferCommand;
import me.crz.minecraft.kingdoms.command.village.VillageHomeCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSetHomeCommand;
import me.crz.minecraft.kingdoms.command.village.VillageLeaderCommand;
import me.crz.minecraft.kingdoms.command.village.money.VillageMoneyCommand;
import me.crz.minecraft.kingdoms.command.village.VillageModeCommand;
import me.crz.minecraft.kingdoms.command.village.VillageMaterialsCommand;
import me.crz.minecraft.kingdoms.command.village.VillageDepositCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.command.village.VillageUpgradeCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSetRankCommand;
import me.crz.minecraft.kingdoms.command.village.VillageInviteCommand;
import me.crz.minecraft.kingdoms.command.village.VillageLeaveMessageCommand;
import me.crz.minecraft.kingdoms.command.village.VillageEnterMessageCommand;
import me.crz.minecraft.kingdoms.command.village.buildNuse.VillageBuildCommand;
import me.crz.minecraft.kingdoms.command.village.VillageJoinCommand;
import me.crz.minecraft.kingdoms.command.village.VillageKickCommand;
import me.crz.minecraft.kingdoms.command.village.VillageLeaveCommand;
import me.crz.minecraft.kingdoms.command.village.VillageMapCommand;
import me.crz.minecraft.kingdoms.command.village.VillageUnclaimCommand;
import me.crz.minecraft.kingdoms.command.village.VillageDisbandCommand;
import me.crz.minecraft.kingdoms.command.village.VillageShowCommand;
import me.crz.minecraft.kingdoms.command.village.VillageClaimCommand;
import me.crz.minecraft.kingdoms.command.village.VillageCreateCommand;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandExecutor;

public class VillageCommand implements CommandExecutor {
    private Map<String, SubCommand> subCommands = new HashMap<>();
    private StringBuilder sb1;
    private StringBuilder sb2;

    public VillageCommand() {
        super();
        (sb1 = new StringBuilder()).append("&8&l--------&r&c&lVillage Commands&r&8&l--------");
        sb1.append("\n&8[&9*&8] &b/{alias} create <name> &7- Creates the settlement");
        sb1.append("\n&8[&9*&8] &b/{alias} show <name> &7- Shows the information of a settlement");
        sb2.append("\n&8[&9*&8] &b/{alias} home <name> &7- Teleport to a settlement's home");
        sb1.append("\n&8[&9*&8] &b/{alias} map &7- Shows virtual map in chat.(hover mouse in map to show name");
        sb1.append("\n&8[&9*&8] &b/{alias} join <name> &7- Joins or accepts the invite from a settlement");
        sb1.append("\n&8[&9*&8] &b/{alias} rules &7- Shows you the rules");
        (sb2 = new StringBuilder("&8&l--------&r&c&lVillage Commands&r&8&l--------")).append("\n&8[&9*&8] &b/{alias} invite <name> &7- Invites the player to the settlement");
        sb2.append("\n&8[&9*&8] &b/{alias} show <name> &7- Shows the information of a settlement");
        sb1.append("\n&8[&9*&8] &b/{alias} invite <name> &7- Invites the player to the settlement");
        sb2.append("\n&8[&9*&8] &b/{alias} map &7- Shows virtual map in chat (hover mouse in map to show name)");
        sb2.append("\n&8[&9*&8] &b/{alias} materials &7- List of requirements to upgrade");
        sb2.append("\n&8[&9*&8] &b/{alias} deposit &7- Deposits resources to upgrade your settlement");
        sb2.append("\n&8[&9*&8] &b/{alias} home [<name>] &7- Teleport to a settlement's home");
        sb2.append("\n&8[&9*&8] &b/{alias} allies &7- Lists all allies");
        sb2.append("\n&8[&9*&8] &b/{alias} ally &7- Adds an ally");
        sb2.append("\n&8[&9*&8] &b/{alias} unally &7- Removes an ally");
        sb2.append("\n&8[&9*&8] &b/{alias} reslist &7- Lists all residents");
        sb2.append("\n&8[&9*&8] &b/{alias} leader help &7- Shows commands for the leaders");
        sb2.append("\n&8[&9*&8] &b/{alias} rules &7- Shows you global settlement rules");
        sb2.append("\n&8[&9*&8] &b/{alias} lock &7- Shows you the vlock commands");
        sb2.append("\n&8[&9*&8] &b/{alias} upkeep &7- Shows you information on your upkeep");
        // TODO: auto-generate/move to messages config
        this.registerCommand(new VillageCreateCommand());
        this.registerCommand(new VillageClaimCommand());
        this.registerCommand(new VillageShowCommand());
        this.registerCommand(new VillageDisbandCommand());
        this.registerCommand(new VillageUnclaimCommand());
        this.registerCommand(new VillageMapCommand());
        this.registerCommand(new VillageLeaveCommand());
        this.registerCommand(new VillageKickCommand());
        this.registerCommand(new VillageJoinCommand());
        this.registerCommand(new VillageBuildCommand());
        this.registerCommand(new VillageEnterMessageCommand());
        this.registerCommand(new VillageLeaveMessageCommand());
        this.registerCommand(new VillageInviteCommand());
        this.registerCommand(new VillageSetRankCommand());
        this.registerCommand(new VillageUpgradeCommand());
        this.registerCommand(new VillagePlotCommand());
        this.registerCommand(new VillageDepositCommand());
        this.registerCommand(new VillageMaterialsCommand());
        this.registerCommand(new VillageModeCommand());
        this.registerCommand(new VillageMoneyCommand());
        this.registerCommand(new VillageLeaderCommand());
        this.registerCommand(new VillageSetAccessCommand());
        this.registerCommand(new VillageSetHomeCommand());
        this.registerCommand(new VillageHomeCommand());
        this.registerCommand(new VillageTransferCommand());
        this.registerCommand(new VillageRenameCommand());
        this.registerCommand(new VillageOutPostCommand());
        this.registerCommand(new VillageRulesCommand());
        this.registerCommand(new VillageLockCommand());
        this.registerCommand(new VillageResListCommand());
        this.registerCommand(new VillageAllyCommand());
        this.registerCommand(new VillageUnAllyCommand());
        this.registerCommand(new VillageAlliesCommand());
        this.registerCommand(new VillageUseCommand());
        this.registerCommand(new VillageSettingsCommand());
        this.registerCommand(new VillageUpkeepCommand());
        this.registerCommand(new VillageBanishCommand());
    }

    public void registerCommand(final SubCommand c) {
        for (final String alias : c.getAliases()) {
            subCommands.put(alias.toLowerCase(), c);
        }
    }

    public void run(final Player p, final String alias, final String[] args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
        if (args.length < 1) {
            MessageUtils.tell(p, (village == null) ? sb1.toString() : sb2.toString(), "alias", alias);
            return;
        }
        final String aliasUsed = args[0].toLowerCase();
        if (!subCommands.containsKey(aliasUsed)) {
            MessageUtils.tell(p, "&c&lUnknown command!");
            return;
        }
        final SubCommand subCmd = subCommands.get(aliasUsed);
        if (p.hasPermission(subCmd.getPermission())) {
            subCmd.run(p, alias, aliasUsed, Arrays.copyOfRange(args, 1, args.length));
            return;
        }
        MessageUtils.tell(p, Message.get("no-permission"));
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (sender instanceof Player) {
            this.run((Player) sender, alias, args);
            return true;
        }
        return false;
    }
}
