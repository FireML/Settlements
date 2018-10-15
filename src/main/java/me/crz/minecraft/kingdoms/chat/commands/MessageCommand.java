package me.crz.minecraft.kingdoms.chat.commands;

import me.crz.minecraft.kingdoms.chat.ChatManager;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MessageCommand extends SubCommand {

    public MessageCommand() {
        super(null, "message <name> <message>", "msg", "tell", "pm", "m", "w", "t", "whisper", "emsg", "etell", "epm", "ewhisper");
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        if (args.length < 2) {
            return false;
        }
        Player target = getPlayer(args[0]);
        if (target == null) {
            MessageUtils.tell(p, getError("player-not-found"), "name", args[0]);
            return true;
        }
        ChatManager.getInstance().sendPrivateMessage(p, target, Arrays.stream(args).skip(1).collect(Collectors.joining(" ")));
        return true;
    }

    public String c(String input) {
        return MessageUtils.replaceColorCodes(input);
    }

    public static Player getPlayer(String name) {
        return Bukkit.getPlayer(name);
    }
}
