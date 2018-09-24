package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.utils.Utils;

import java.util.Arrays;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAdminWorldCommand extends SubCommand {
    public VillageAdminWorldCommand() {
        super("Sets if a world is claimable in or not", "world");
        this.setPermission("village.admin");
        this.setUsage("world <yes|no>");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base), null, null);
            return;
        }
        if (Arrays.asList("yes", "no").contains(args[0])) {
            String option;
            if (args[0].equalsIgnoreCase("yes")) {
                option = "enabled";
                Utils.removeDisabledWorld(player.getWorld());
            } else {
                option = "disabled";
                Utils.addDisabledWorld(player.getWorld());
            }
            MessageUtils.tell(player, "&c&lClaiming has been &r&6&l" + option + "&r&c&l for world &r&6&l" + player.getWorld().getName(), null, null);
        } else {
            MessageUtils.tell(player, this.getUsage(base), null, null);
        }
    }
}
