package me.crz.minecraft.kingdoms.chat.commands;

import me.crz.minecraft.kingdoms.utils.config.Data;
import me.crz.minecraft.kingdoms.village.Resident;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import me.crz.minecraft.kingdoms.KingdomsPlugin;

import java.text.DecimalFormat;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

import me.crz.minecraft.kingdoms.essentials.command.Command;

public class MessageCommand extends Command {
    private static Map<UUID, UUID> lastMessage;

    static {
        lastMessage = new HashMap<>();
    }

    public MessageCommand() {
        super("", "message", "msg", "message", "tell", "pm", "m", "w", "t", "whisper", "emsg", "etell", "epm", "ewhisper");
        this.setUsage("<name> <message>");
    }

    public void run(Player p, String alias, String[] args) {
        StringBuilder sb = new StringBuilder();
        if (args.length < 2) {
            p.sendMessage(this.getUsage());
            return;
        }
        for (int i = 1; i < args.length; ++i) {
            sb.append(args[i]);
            sb.append(" ");
        }
        Player target = getPlayer(args[0]);
        if (target == null) {
            p.sendMessage(String.valueOf(args[0]) + " isn't online");
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(p);
        Resident tres = ResidentManager.getInstance().getResident(target);
        if (res.getIgnore().contains(tres.getUUID())) {
            MessageUtils.tell(p, "&cYou cannot message &6" + target.getName() + "&c because you are ignoring them! \n&6/ignore " + target.getName() + "&c to un-ignore them");
            return;
        }
        if (tres.getIgnore().contains(res.getUUID())) {
            MessageUtils.tell(p, "&cYou cannot message &6" + target.getName() + "&c because they are ignoring you!");
            return;
        }

        DecimalFormat df = new DecimalFormat("#,###.##");
        String senderHoverText = "&9Name: &7" + p.getDisplayName()
                + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(p));
        if (res.getVillage() != null) {
            senderHoverText += "\n&9" + StringUtils.capitalize(res.getVillage().getState().toString().toLowerCase()) + ": &7" + res.getVillage().getName();
        }
        String targetHoverText = "&9Name: &7" + target.getDisplayName()
                + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(target));
        if (tres.getVillage() != null) {
            targetHoverText += "\n&9" + StringUtils.capitalize(tres.getVillage().getState().toString().toLowerCase()) + ": &7" + tres.getVillage().getName();
        }
        String playerPrefix = KingdomsPlugin.chat.getPlayerPrefix(p);
        String targetPrefix = KingdomsPlugin.chat.getPlayerPrefix(target);
        TextComponent from = new TextComponent(c("&8["));
        from.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/r"));
        TextComponent fromYou = new TextComponent(c("&ayou"));
        fromYou.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(c(senderHoverText)).create()));
        from.addExtra(fromYou);
        from.addExtra(new TextComponent(c(" &7\u27a6 &3")));
        TextComponent fromTo = new TextComponent(c(String.valueOf(targetPrefix) + target.getDisplayName()));
        fromTo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(c(targetHoverText)).create()));
        from.addExtra(fromTo);
        from.addExtra(c("&8] "));
        TextComponent temp = new TextComponent(sb.toString());
        temp.setColor(ChatColor.AQUA);
        from.addExtra(temp);
        TextComponent to = new TextComponent(c("&8["));
        to.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/r"));
        TextComponent toYou = new TextComponent(c("&ayou"));
        toYou.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(c(targetHoverText)).create()));
        TextComponent toTo = new TextComponent(c(String.valueOf(playerPrefix) + p.getDisplayName()));
        toTo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(c(senderHoverText)).create()));
        to.addExtra(toTo);
        to.addExtra(new TextComponent(c(" &7\u27a6 &3")));
        to.addExtra(toYou);
        to.addExtra(c("&8] "));
        to.addExtra(temp);
        p.spigot().sendMessage(from);
        target.spigot().sendMessage(to);
        lastMessage.put(p.getUniqueId(), target.getUniqueId());
        lastMessage.put(target.getUniqueId(), p.getUniqueId());
        StringBuilder msg = new StringBuilder("&7[&6SS&7]&r ");
        msg.append(playerPrefix);
        msg.append(p.getDisplayName());
        msg.append(c(" &7\u27a6 &3"));
        msg.append(targetPrefix);
        msg.append(target.getDisplayName());
        msg.append("&7: ");
        msg.append(sb.toString());
        for (String sUUID : Data.socialSpy) {
            Player pl = Bukkit.getPlayer(UUID.fromString(sUUID));
            if (pl != null && pl.isOnline()) {
                MessageUtils.tell(pl, msg.toString());
            }
        }
    }

    public String c(String input) {
        return MessageUtils.replaceColorCodes(input);
    }

    public static Player getPlayer(String name) {
        return Bukkit.getPlayer(name);
    }

    public static void setLastMessage(UUID uuid1, UUID uuid2) {
        lastMessage.put(uuid1, uuid2);
        lastMessage.put(uuid2, uuid1);
    }

    public static Player getLastMessage(Player p) {
        if (lastMessage.containsKey(p.getUniqueId())) {
            return Bukkit.getPlayer(lastMessage.get(p.getUniqueId()));
        }
        return null;
    }
}
