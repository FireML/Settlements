package de.themoep.settlements.command.admin;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.SettlementState;
import org.bukkit.command.CommandSender;

public class AdminLevelCommand extends SubCommand {
    public AdminLevelCommand(SubCommand parent) {
        super(parent, "level <settlement> village|kingdom <level> [<triggerevents>]", "setlevel", "state", "setstate");
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

            if (SettlementManager.getInstance().getLevel(state, level).isEmpty()) {
                getError("no-level").tell(sender, "level", String.valueOf(level));
                return true;
            }

            settlement.setState(state);
            settlement.setLevel(level);

            sender.sendMessage(SettlementsPlugin.getInstance().messageUtils.toComponent("&cSet " + settlement.getName() + " to " + state.getTag() + "/" + level));

            if (args.length < 4 || !"false".equalsIgnoreCase(args[3])) {
                SettlementManager.getInstance().onLevelUp(settlement, settlement.getChief());
                sender.sendMessage(SettlementsPlugin.getInstance().messageUtils.toComponent("&cTriggered level up events!"));
            }
            settlement.save();
        } catch (NumberFormatException e) {
            getError("not-a-number").tell(sender, "input", args[2]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
