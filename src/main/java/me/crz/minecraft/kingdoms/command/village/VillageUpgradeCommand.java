package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageUpgradeCommand extends SubCommand {
    public VillageUpgradeCommand() {
        super("Upgrade your village to a kingdom", "upgrade");
        this.setPermission("kingdoms.user");
        this.setUsage("upgrade");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        MessageUtils.tell(player, "&c&lThis command is disabled until &6&lfull release&c&l!s", null, null);
    }
}
