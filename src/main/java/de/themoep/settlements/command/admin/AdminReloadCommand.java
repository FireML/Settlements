package de.themoep.settlements.command.admin;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.integration.IntegrationManager;
import de.themoep.settlements.utils.config.ConfigManager;
import org.bukkit.command.CommandSender;

public class AdminReloadCommand extends SubCommand {
    public AdminReloadCommand(AdminCommand parent) {
        super(parent, "reload");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        ConfigManager.getInstance().getDataConfig().saveConfig();
        ConfigManager.getInstance().reload(true);
        IntegrationManager.getInstance().reload();
        getMessage("success").tell(sender);
        return true;
    }
}
