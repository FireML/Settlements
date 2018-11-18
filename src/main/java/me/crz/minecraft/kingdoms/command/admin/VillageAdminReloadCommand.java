package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import org.bukkit.command.CommandSender;

public class VillageAdminReloadCommand extends SubCommand {
    public VillageAdminReloadCommand(VillageAdminCommand parent) {
        super(parent, "reload");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        ConfigManager.getInstance().reload();
        getMessage("success").tell(sender);
        return true;
    }
}
