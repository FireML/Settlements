package de.themoep.settlements.command.admin;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import org.bukkit.command.CommandSender;

public class VillageAdminConvertConfigCommand extends SubCommand {
    public VillageAdminConvertConfigCommand(VillageAdminCommand parent) {
        super(parent, "convertconfig <toversion>");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        try {
            int toVersion = Integer.parseInt(args[0]);
            int oldVersion = Config.CONFIG_VERSION;
            if (toVersion == oldVersion) {
                getError("same-version").tell(sender, "version", String.valueOf(toVersion));
            } else {
                ConfigManager.getInstance().convertConfigTo(toVersion);
                getMessage("success").tell(sender,
                        "from", String.valueOf(oldVersion),
                        "to", String.valueOf(toVersion)
                );
            }
        } catch (NumberFormatException e) {
            getError("not-a-number").tell(sender, "input", args[0]);
        }
        return true;
    }
}
