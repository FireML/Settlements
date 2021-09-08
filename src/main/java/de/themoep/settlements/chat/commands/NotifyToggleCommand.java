package de.themoep.settlements.chat.commands;

import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;

public class NotifyToggleCommand extends ResidentSubCommand {

    public NotifyToggleCommand() {
        super(null, "notify [on|off]", "notifytoggle", "togglenotify", "nottoggle", "notiftoggle");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        boolean toggle = !res.isNotifyEnabled();
        if (args.length > 0) {
            try {
                toggle = Utils.parseBoolean(args[0]);
            } catch (IllegalArgumentException e) {
                getError("invalid-option").tell(res, "input", args[0]);
                return false;
            }
        }
        res.setNotifyEnabled(toggle);
        if (toggle) {
            getMessage("enabled").tell(res);
        } else {
            getMessage("disabled").tell(res);
        }
        return true;
    }
}
