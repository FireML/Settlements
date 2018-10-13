package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.config.Data;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAdminSocialSpyCommand extends SubCommand {
    public VillageAdminSocialSpyCommand(SubCommand parent) {
        super(parent, "ss", "socialspy");
        setPermission("kingdoms.admin.ss");
    }

    @Override
    public boolean run(final Player player, final String base, final String alias, final String... args) {
        if (Data.socialSpy.contains(player.getUniqueId().toString())) {
            Data.socialSpy.remove(player.getUniqueId().toString());
            MessageUtils.tell(player, "&6&lSocialSpy &c&lDisabled");
        } else {
            Data.socialSpy.add(player.getUniqueId().toString());
            MessageUtils.tell(player, "&6&lSocialSpy &a&lEnabled");
        }
        return true;
    }
}
