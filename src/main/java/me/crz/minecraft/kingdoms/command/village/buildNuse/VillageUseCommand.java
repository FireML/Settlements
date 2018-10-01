package me.crz.minecraft.kingdoms.command.village.buildNuse;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.BuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.landflags.UseGui;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageUseCommand extends SubCommand {
    public VillageUseCommand() {
        super("Set who can use stuff in your village", "use");
        this.setPermission("kingdoms.user");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT);
            return;
        }
        if (!this.canChange(village, player)) {
            MessageUtils.tell(player, MessageUtils.Message.NO_PERMISSION);
            return;
        }
        if (args.length < 2) {
            UseGui.getInstance().open(player);
            return;
        }
        try {
            Use u = Use.valueOf(args[0].toUpperCase());
            BuildState state = BuildState.valueOf(args[1].toUpperCase());
            village.setUseState(u, state);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
            MessageUtils.tell(player, "&7State of &6" + u.name() + " &7set to &6" + state.name());
        } catch (IllegalArgumentException e) {
            MessageUtils.tell(player, "&c/s use <name> <buildState>");            
        }
    }

    private boolean canChange(Village village, Player player) {
        return village.getChief().equals(player.getUniqueId());
    }
}
