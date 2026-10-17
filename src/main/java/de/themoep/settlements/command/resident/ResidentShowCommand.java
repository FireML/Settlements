package de.themoep.settlements.command.resident;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResidentShowCommand extends SubCommand {

    public ResidentShowCommand(SubCommand parent) {
        super(parent, "show <name>", "data", "info");
        setVisible(false);
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 1 && !(sender instanceof Player)) {
            return false;
        }
        Utils.runAsync(() -> {
            Resident target;
            if (args.length > 0) {
                target = ResidentManager.getInstance().getResident(args[0]);
                if (target == null) {
                    MessageUtils.tell(sender, getError("player-not-found").getReplacedComponent("name", args[0]));
                    return;
                }
            } else {
                target = ResidentManager.getInstance().getResident((Player) sender);
            }

            getMessage("info." + (target.hasSettlement() ? "in-settlement" : "free")).tell(sender,
                    target.getReplacements()
            );
        });
        return true;
    }
}
