package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAlliesCommand extends SubCommand {
    public VillageAlliesCommand() {
        super("lists all your allies", "allies");
        this.setPermission("village.user");
    }

    @Override
    public void run(final Player p, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
        if (village == null) {
            MessageUtils.tell(p, MessageUtils.NOT_IN_CURRENT_MESSAGE);
            return;
        }
        final StringBuilder sb = new StringBuilder("&8&l--------&r&c&lAllies&r&8&l--------\n");
        for (final String ally : village.getAllies()) {
            sb.append("&a");
            sb.append(ally);
            sb.append("&7, ");
        }
        MessageUtils.tell(p, sb.toString());
    }
}
