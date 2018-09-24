package me.crz.minecraft.kingdoms.command.village.buildNuse;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.BuildState;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import java.util.Arrays;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageBuildCommand extends SubCommand
{
    public VillageBuildCommand() {
        super("Set who can build in your village", new String[] { "build" });
        this.setPermission("village.user");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 1) {
            MessageUtils.tellList(player, Arrays.<String>asList("&8&l-------- &r&c&l/" + base + " " + alias + "&r " + "&8&l--------", "&8[&9*&8] &b/" + base + " " + alias + " residents &7- residents and officers to build in claimed land", "&8[&9*&8] &b/" + base + " " + alias + " officers &7- only officers are allowed to build in claimed land", "&8[&9*&8] &b/" + base + " " + alias + " public &7- allows everyone to build in your claimed land", "&8[&9*&8] &b/" + base + " " + alias + " ally &7- allows your allies to build in your claimed land"));
            return;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE);
            return;
        }
        if (!this.canChange(village, player)) {
            MessageUtils.tell(player, MessageUtils.NO_PERMISSION_MESSAGE);
            return;
        }
        if (args[0].equalsIgnoreCase("residents") || args[0].equalsIgnoreCase("officers") || args[0].equalsIgnoreCase("public") || args[0].equalsIgnoreCase("ally")) {
            final BuildState newState = BuildState.valueOf(args[0].toUpperCase());
            village.setBuildState(newState);
            VillageManager.getInstance().saveVillage(village);
            MessageUtils.tell(player, MessageUtils.SUCCESS_BUILD_STATE_MESSAGE, "{state}", args[0]);
            return;
        }
        MessageUtils.tellList(player, Arrays.<String>asList("&8&l-------- &r&c&l/" + base + " " + alias + "&r " + "&8&l--------", "&8[&9*&8] &b/" + base + " " + alias + " residents &7- residents and officers to build in claimed land" + "&8[&9*&8] &b/" + base + " " + alias + " officers &7- only officers are allowed to build in claimed land" + "&8[&9*&8] &b/" + base + " " + alias + " public &7- allows everyone to build in your claimed land"));
    }
    
    private boolean canChange(final Village village, final Player player) {
        return village.getChief().equals(player.getUniqueId());
    }
}
