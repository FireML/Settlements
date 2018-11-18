package me.crz.minecraft.kingdoms.chat.commands;

import me.crz.minecraft.kingdoms.chat.ChatManager;
import me.crz.minecraft.kingdoms.command.PlayerSubCommand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
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
            MessageUtils.tell(p, getMessage("no-one-messaged"));
        } else {
            Player target = Bukkit.getPlayer(targetId);
            if (target == null) {
                MessageUtils.tell(p, getMessage("target-is-offline"));
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
