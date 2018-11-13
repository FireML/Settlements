package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class VillageAdminLevelCommand extends SubCommand {
    public VillageAdminLevelCommand(SubCommand parent) {
        super(parent, "level <settlement> village|kingdom <level>", "setlevel", "state", "setstate");
        setPermission("kingdoms.admin");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (args.length < 3) {
            return false;
        }
        Village village = VillageManager.getInstance().getVillageFromName(args[0]);
        if (village == null) {
            getError("village-doesnt-exist").tell(player, "name", args[0]);
            return true;
        }
        try {
            VillageState state = VillageState.valueOf(args[1].toUpperCase());
            int level = Integer.parseInt(args[2]);

            village.setState(state);
            village.setLevel(level);
            village.getVault().clear();

            player.sendMessage(ChatColor.RED + "Set " + village.getName() + " to " + state.getTag() + "/" + level);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
        } catch (NumberFormatException e) {
            getError("not-a-number").tell(player, "input", args[2]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
