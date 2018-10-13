package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageRulesCommand extends SubCommand {
    public VillageRulesCommand(SubCommand parent) {
        super(parent, "rules");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        MessageUtils.tell(player, Message.get("server-rules"));
        return true;
    }
}
