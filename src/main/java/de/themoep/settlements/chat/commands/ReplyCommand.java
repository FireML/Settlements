package de.themoep.settlements.chat.commands;

import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.chat.ChatManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ReplyCommand extends PlayerSubCommand {
    public ReplyCommand() {
        super(null, "reply <message>", "r");
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        UUID targetId = ChatManager.getInstance().getLastMessaged(p);
        if (targetId == null) {
            getMessage("no-one-messaged").tell(p);
        } else {
            Player target = Bukkit.getPlayer(targetId);
            if (target == null) {
                getMessage("target-is-offline").tell(p);
            } else {
                ChatManager.getInstance().sendPrivateMessage(p, target, String.join(" ", args));
            }
        }
        return true;
    }

    public String c(String input) {
        return MessageUtils.replaceColorCodes(input);
    }
}
