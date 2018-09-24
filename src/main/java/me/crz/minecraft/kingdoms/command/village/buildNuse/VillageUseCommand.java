package me.crz.minecraft.kingdoms.command.village.buildNuse;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.BuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.landflags.UseGui;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageUseCommand extends SubCommand
{
    public VillageUseCommand() {
        super("Set who can use stuff in your village", new String[] { "use" });
        this.setPermission("village.user");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE);
            return;
        }
        if (!this.canChange(village, player)) {
            MessageUtils.tell(player, MessageUtils.NO_PERMISSION_MESSAGE);
            return;
        }
        if (args.length < 2) {
            UseGui.getInstance().open(player);
            return;
        }
        Use[] values;
        for (int length = (values = Use.values()).length, i = 0; i < length; ++i) {
            final Use u = values[i];
            if (u.name().equalsIgnoreCase(args[0])) {
                BuildState[] values2;
                for (int length2 = (values2 = BuildState.values()).length, j = 0; j < length2; ++j) {
                    final BuildState state = values2[j];
                    if (state.name().equalsIgnoreCase(args[1])) {
                        village.setUseState(u, state);
                        VillageManager.getInstance().saveVillage(village);
                        MessageUtils.tell(player, "&7State of &6" + u.name() + " &7set to &6" + state.name());
                        return;
                    }
                }
            }
        }
        MessageUtils.tell(player, "&c/s use <name> <buildState>");
    }
    
    private boolean canChange(final Village village, final Player player) {
        return village.getChief().equals(player.getUniqueId());
    }
}
