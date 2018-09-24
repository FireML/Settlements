package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAdminSocialSpuCommand extends SubCommand
{
    public VillageAdminSocialSpuCommand() {
        super("Lets you see other peoples messages", new String[] { "ss", "socialspy" });
        this.setPermission("village.admin.ss");
        this.setUsage("socialspy");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (Config.socialSpy.contains(player.getUniqueId().toString())) {
            Config.socialSpy.remove(player.getUniqueId().toString());
            MessageUtils.tell(player, "&6&lSocialSpy &c&lDisabled");
        }
        else {
            Config.socialSpy.add(player.getUniqueId().toString());
            MessageUtils.tell(player, "&6&lSocialSpy &a&lEnabled");
        }
    }
}
