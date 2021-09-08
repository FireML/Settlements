package de.themoep.settlements.command.admin;

import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.village.VillageState;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class VillageAdminLevelCommand extends SubCommand {
    public VillageAdminLevelCommand(SubCommand parent) {
        super(parent, "level <settlement> village|kingdom <level>", "setlevel", "state", "setstate");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 3) {
            return false;
        }
        Village village = VillageManager.getInstance().getFromName(args[0]);
        if (village == null) {
            getError("village-doesnt-exist").tell(sender, "name", args[0]);
            return true;
        }
        try {
            VillageState state = VillageState.valueOf(args[1].toUpperCase());
            int level = Integer.parseInt(args[2]);

            village.setState(state);
            village.setLevel(level);
            village.getVault().clear();

            sender.sendMessage(ChatColor.RED + "Set " + village.getName() + " to " + state.getTag() + "/" + level);
            DatabaseManager.getInstance().saveSettlement(village);
        } catch (NumberFormatException e) {
            getError("not-a-number").tell(sender, "input", args[2]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
