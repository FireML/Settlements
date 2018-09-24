package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageRulesCommand extends SubCommand
{
    public VillageRulesCommand() {
        super("Links you to the rules", new String[] { "rules" });
        this.setPermission("village.user");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        MessageUtils.tell(player, "&6&lhttps://www.mc-wolfpack.com/forum/m/48851490/viewthread/31928866-server-rules");
    }
}
