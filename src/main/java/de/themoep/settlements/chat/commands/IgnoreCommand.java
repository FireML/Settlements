package de.themoep.settlements.chat.commands;

import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.db.DatabaseManager;

import java.util.stream.Collectors;

public class IgnoreCommand extends ResidentSubCommand {

    public IgnoreCommand() {
        super(null, "ignore list|<player>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            if (args[0].equalsIgnoreCase("list")) {
                getMessage("list.head").tell(res);
                res.tell(res.getIgnore().stream()
                        .map(id -> {
                            Resident resident = ResidentManager.getInstance().getResident(id);
                            if (resident != null) {
                                return getMessage("list.entry").getReplacedText("name", resident.getName());
                            } else {
                                return getMessage("list.entry").getReplacedText("name", id.toString());
                            }
                        }).collect(Collectors.joining(getText("list.separator")))
                );
                return;
            }
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target != null) {
                if (res.getIgnore().contains(target.getUUID())) {
                    res.removeIgnore(target.getUUID());
                    getMessage("unignored").tell(res, "name", target.getName());
                } else {
                    res.addIgnore(target.getUUID());
                    getMessage("ignored").tell(res, "name", target.getName());
                }
                DatabaseManager.getInstance().saveResident(res);
                return;
            }
            getError("player-not-found").tell(res, "name", args[0]);
        });
        return true;
    }
}
