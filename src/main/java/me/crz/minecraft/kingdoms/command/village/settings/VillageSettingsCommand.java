package me.crz.minecraft.kingdoms.command.village.settings;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageSettingsCommand extends SubCommand {
    public VillageSettingsCommand() {
        super("Shows a settings gui", "settings");
        this.setPermission("kingdoms.user");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT);
            return;
        }
        if (!this.canChange(village, player)) {
            MessageUtils.tell(player, MessageUtils.Message.NO_PERMISSION);
            return;
        }
        SettingsGUI.getInstance().open(player);
    }

    private boolean canChange(final Village village, final Player player) {
        return village.getChief().equals(player.getUniqueId());
    }
}
