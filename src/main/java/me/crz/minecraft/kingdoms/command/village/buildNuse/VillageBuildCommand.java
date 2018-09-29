package me.crz.minecraft.kingdoms.command.village.buildNuse;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.BuildState;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;

import java.util.Arrays;

import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageBuildCommand extends SubCommand {
    public VillageBuildCommand() {
        super("Set who can build in your village", "build");
        this.setPermission("village.user");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tellList(player, Arrays.asList("&8&l-------- &r&c&l/" + base + " " + alias + "&r " + "&8&l--------", "&8[&9*&8] &b/" + base + " " + alias + " residents &7- residents and officers to build in claimed land", "&8[&9*&8] &b/" + base + " " + alias + " officers &7- only officers are allowed to build in claimed land", "&8[&9*&8] &b/" + base + " " + alias + " public &7- allows everyone to build in your claimed land", "&8[&9*&8] &b/" + base + " " + alias + " ally &7- allows your allies to build in your claimed land"));
            return;
        }
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT);
            return;
        }
        if (!this.canChange(village, player)) {
            MessageUtils.tell(player, MessageUtils.Message.NO_PERMISSION);
            return;
        }
        if (args[0].equalsIgnoreCase("residents") || args[0].equalsIgnoreCase("officers") || args[0].equalsIgnoreCase("public") || args[0].equalsIgnoreCase("ally")) {
            BuildState newState = BuildState.valueOf(args[0].toUpperCase());
            village.setBuildState(newState);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
            MessageUtils.tell(player, MessageUtils.Message.SUCCESS_BUILD_STATE, "{state}", args[0]);
            return;
        }
        MessageUtils.tellList(player, Arrays.asList("&8&l-------- &r&c&l/" + base + " " + alias + "&r " + "&8&l--------", "&8[&9*&8] &b/" + base + " " + alias + " residents &7- residents and officers to build in claimed land" + "&8[&9*&8] &b/" + base + " " + alias + " officers &7- only officers are allowed to build in claimed land" + "&8[&9*&8] &b/" + base + " " + alias + " public &7- allows everyone to build in your claimed land"));
    }

    private boolean canChange(Village village, Player player) {
        return village.getChief().equals(player.getUniqueId());
    }
}
