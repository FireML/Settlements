package me.crz.minecraft.kingdoms.chat.commands;

import me.crz.minecraft.kingdoms.utils.config.Data;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.text.DecimalFormat;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.essentials.command.Command;

public class ReplyCommand extends Command {
    public ReplyCommand() {
        super("Replys to the last person who messaged you.", "reply", "r");
        this.setUsage("<message>");
    }

    public void run(Player p, String alias, String[] args) {
        StringBuilder sb = new StringBuilder();
        if (args.length < 1) {
            p.sendMessage(this.getUsage());
            return;
        }
        for (int i = 0; i < args.length; ++i) {
            sb.append(args[i]);
            sb.append(" ");
        }
        Player target = MessageCommand.getLastMessage(p);
        if (target != null) {
            if (!target.getPlayer().isOnline()) {
                p.sendMessage(this.c("�4The person you are trying to reply to is currently offline!"));
            } else {
                Resident res = ResidentManager.getInstance().getResident(p);
                Resident tres = ResidentManager.getInstance().getResident(target);
                if (res.getIgnore().contains(tres.getUUID().toString())) {
                    MessageUtils.tell(p, "&cYou cannot message &6" + target.getName() + "&c because you are ignoring them! \n&6/ignore " + target.getName() + "&c to un-ignore them");
                    return;
                }
                if (tres.getIgnore().contains(res.getUUID().toString())) {
                    MessageUtils.tell(p, "&cYou cannot message &6" + target.getName() + "&c because they are ignoring you!");
                    return;
                }
                String playerPrefix = KingdomsPlugin.chat.getPlayerPrefix(p);
                String targetPrefix = KingdomsPlugin.chat.getPlayerPrefix(target);
                DecimalFormat df = new DecimalFormat("#,###.##");
                TextComponent from = new TextComponent(this.c("&8["));
                from.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/r"));
                TextComponent fromYou = new TextComponent(this.c("&ayou"));
                fromYou.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.c("&9Name: &7" + p.getDisplayName() + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(p)))).create()));
                from.addExtra(fromYou);
                from.addExtra(new TextComponent(this.c(" &7\u27a6 &3")));
                TextComponent fromTo = new TextComponent(this.c(String.valueOf(targetPrefix) + target.getDisplayName()));
                fromTo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.c("&9Name: &7" + target.getDisplayName() + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(target)))).create()));
                from.addExtra(fromTo);
                from.addExtra(this.c("&8] "));
                TextComponent temp = new TextComponent(sb.toString());
                temp.setColor(ChatColor.AQUA);
                from.addExtra(temp);
                TextComponent to = new TextComponent(this.c("&8["));
                to.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/r"));
                TextComponent toYou = new TextComponent(this.c("&ayou"));
                toYou.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.c("&9Name: &7" + target.getDisplayName() + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(target)))).create()));
                TextComponent toTo = new TextComponent(this.c(String.valueOf(playerPrefix) + p.getDisplayName()));
                toTo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.c("&9Name: &7" + p.getDisplayName() + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(p)))).create()));
                to.addExtra(toTo);
                to.addExtra(new TextComponent(this.c(" &7\u27a6 &3")));
                to.addExtra(toYou);
                to.addExtra(this.c("&8] "));
                to.addExtra(temp);
                p.spigot().sendMessage(from);
                target.spigot().sendMessage(to);
                MessageCommand.setLastMessage(p.getUniqueId(), target.getUniqueId());
                StringBuilder msg = new StringBuilder("&7[&6SS&7]&r ");
                msg.append(playerPrefix);
                msg.append(p.getDisplayName());
                msg.append(this.c(" &7\u27a6 &3"));
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
        } else {
            p.sendMessage(this.c("�4You have no one to reply to."));
        }
    }

    public String c(String input) {
        return MessageUtils.replaceColorCodes(input);
    }
}
