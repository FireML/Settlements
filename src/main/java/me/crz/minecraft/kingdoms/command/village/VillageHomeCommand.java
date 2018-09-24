package me.crz.minecraft.kingdoms.command.village;

import org.bukkit.Location;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.ExecuteMoveUtil;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageHomeCommand extends SubCommand {
    public VillageHomeCommand() {
        super("Teleport to village home", "home");
        this.setPermission("village.user");
        this.setUsage("home");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        final Location loc = village.getVillageHome();
        if (loc != null) {
            MessageUtils.tell(player, "&e&lTeleporting in 5 seconds...");
            ExecuteMoveUtil.getInstance().execute(player, () -> player.teleport(loc), 3, "Teleport");
        } else {
            MessageUtils.tell(player, "&c&lYour village doesn't have a home ask your " + VillageRank.CHIEF.getTag(village.getState()) + " to set one!");
        }
    }
}
