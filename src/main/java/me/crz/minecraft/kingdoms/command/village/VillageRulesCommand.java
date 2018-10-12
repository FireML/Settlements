package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageRulesCommand extends SubCommand {
    public VillageRulesCommand() {
        super("Links you to the rules", "rules");
        this.setPermission("kingdoms.user");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        MessageUtils.tell(player, Message.get("server-rules"));
    }
}
