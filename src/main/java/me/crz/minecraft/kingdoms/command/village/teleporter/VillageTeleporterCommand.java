package me.crz.minecraft.kingdoms.command.village.teleporter;

import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.Arrays;

import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageTeleporterCommand extends SubCommand {
    private Map<String, SubCommand> subCommands;
    private String help;

    public VillageTeleporterCommand() {
        super("configure the teleporter", "teleporter");
        subCommands = new HashMap<>();
        this.setPermission("village.user");
        this.setUsage("teleporter <set|cost> <args>");
        this.registerCommand(new VillageTeleporterSetCommand());
        this.registerCommand(new VillageTeleporterCostCommand());
        final StringBuilder sb = new StringBuilder("&8&l--------&r&c&lVillage Teleporter Commands&r&8&l--------");
        for (final SubCommand subCmd : subCommands.values()) {
            sb.append("\n&c/{base} ").append(subCmd.getBaseAlias()).append(": ").append(subCmd.getDescription());
        }
        help = sb.toString();
    }

    public void registerCommand(final SubCommand c) {
        for (final String alias : c.getAliases()) {
            subCommands.put(alias.toLowerCase(), c);
        }
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, help, "{base}", base);
            return;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF)) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + "&r&c&l!", null, null);
            return;
        }
        if (village.getLevel() < 4 || !village.getState().equals(VillageState.KINGDOM)) {
            MessageUtils.tell(player, "&c&lYou require &r&6&lKingdom Level 4 or above&r&c&l to use this command!", null, null);
            return;
        }
        final String aliasUsed = args[0].toLowerCase();
        if (subCommands.containsKey(aliasUsed)) {
            subCommands.get(aliasUsed).run(player, alias, aliasUsed, Arrays.copyOfRange(args, 1, args.length));
            return;
        }
        MessageUtils.tell(player, "&c&lUnknown command!", null, null);
    }
}
