package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.utils.Utils;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAdminWorldCommand extends SubCommand {
    public VillageAdminWorldCommand(SubCommand parent) {
        super(parent, "world <yes|no>");
        setPermission("kingdoms.admin");
    }

    @Override
    public boolean run(final Player player, final String base, final String alias, final String... args) {
        if (args.length > 0 && (args[0].equalsIgnoreCase("yes") || args[0].equalsIgnoreCase("no"))) {
            String option;
            if (args[0].equalsIgnoreCase("yes")) {
                option = "enabled";
                Utils.removeDisabledWorld(player.getWorld());
            } else {
                option = "disabled";
                Utils.addDisabledWorld(player.getWorld());
            }
            MessageUtils.tell(player, "&c&lClaiming has been &r&6&l" + option + "&r&c&l for world &r&6&l" + player.getWorld().getName());
            return true;
        }
        return false;
    }
}
