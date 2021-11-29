package de.themoep.settlements.command.admin;

import de.themoep.settlements.village.Settlement;
import de.themoep.settlements.village.SettlementManager;
import de.themoep.settlements.village.SettlementState;
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
        Settlement settlement = SettlementManager.getInstance().getFromName(args[0]);
        if (settlement == null) {
            getError("settlement-doesnt-exist").tell(sender, "name", args[0]);
            return true;
        }
        try {
            SettlementState state = SettlementState.valueOf(args[1].toUpperCase());
            int level = Integer.parseInt(args[2]);

            settlement.setState(state);
            settlement.setLevel(level);
            settlement.getVault().clear();

            sender.sendMessage(ChatColor.RED + "Set " + settlement.getName() + " to " + state.getTag() + "/" + level);
            DatabaseManager.getInstance().saveSettlement(settlement);
        } catch (NumberFormatException e) {
            getError("not-a-number").tell(sender, "input", args[2]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
