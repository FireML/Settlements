package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageCopy extends SubCommand {
    public VillageCopy() {
        super("", "");
        this.setPermission("village.user");
        this.setUsage("");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base), null, null);
        }
    }
}
