package me.crz.minecraft.kingdoms.chat.commands;

import me.crz.minecraft.kingdoms.village.Resident;

import java.util.UUID;

import me.crz.minecraft.kingdoms.utils.config.Config;
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

    public void run(final Player p, final String alias, final String[] args) {
        final StringBuilder sb = new StringBuilder();
        if (args.length < 1) {
            p.sendMessage(this.getUsage());
            return;
        }
        for (int i = 0; i < args.length; ++i) {
            sb.append(args[i]);
            sb.append(" ");
        }
        final Player target = MessageCommand.getLastMessage(p);
        if (target != null) {
            if (!target.getPlayer().isOnline()) {
                p.sendMessage(this.c("�4The person you are trying to reply to is currently offline!"));
            } else {
                final Resident res = ResidentManager.getInstance().getResident(p);
                final Resident tres = ResidentManager.getInstance().getResident(target);
                if (res.getIgnore().contains(tres.getUUID().toString())) {
                    MessageUtils.tell(p, "&cYou cannot message &6" + target.getName() + "&c because you are ignoring them! \n&6/ignore " + target.getName() + "&c to un-ignore them");
                    return;
                }
                if (tres.getIgnore().contains(res.getUUID().toString())) {
                    MessageUtils.tell(p, "&cYou cannot message &6" + target.getName() + "&c because they are ignoring you!");
                    return;
                }
                final String playerPrefix = KingdomsPlugin.chat.getPlayerPrefix(p);
                final String targetPrefix = KingdomsPlugin.chat.getPlayerPrefix(target);
                final DecimalFormat df = new DecimalFormat("#,###.##");
                final TextComponent from = new TextComponent(this.c("&8["));
                from.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/r"));
                final TextComponent fromYou = new TextComponent(this.c("&ayou"));
                fromYou.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.c("&9Name: &7" + p.getDisplayName() + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(Bukkit.getOfflinePlayer(p.getUniqueId()))))).create()));
                from.addExtra(fromYou);
                from.addExtra(new TextComponent(this.c(" &7\u27a6 &3")));
                final TextComponent fromTo = new TextComponent(this.c(String.valueOf(targetPrefix) + target.getDisplayName()));
                fromTo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.c("&9Name: &7" + target.getDisplayName() + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(Bukkit.getOfflinePlayer(target.getUniqueId()))))).create()));
                from.addExtra(fromTo);
                from.addExtra(this.c("&8] "));
                final TextComponent temp = new TextComponent(sb.toString());
                temp.setColor(ChatColor.AQUA);
                from.addExtra(temp);
                final TextComponent to = new TextComponent(this.c("&8["));
                to.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/r"));
                final TextComponent toYou = new TextComponent(this.c("&ayou"));
                toYou.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.c("&9Name: &7" + target.getDisplayName() + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(Bukkit.getOfflinePlayer(target.getUniqueId()))))).create()));
                final TextComponent toTo = new TextComponent(this.c(String.valueOf(playerPrefix) + p.getDisplayName()));
                toTo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.c("&9Name: &7" + p.getDisplayName() + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(Bukkit.getOfflinePlayer(p.getUniqueId()))))).create()));
                to.addExtra(toTo);
                to.addExtra(new TextComponent(this.c(" &7\u27a6 &3")));
                to.addExtra(toYou);
                to.addExtra(this.c("&8] "));
                to.addExtra(temp);
                p.spigot().sendMessage(from);
                target.spigot().sendMessage(to);
                MessageCommand.setLastMessage(p.getUniqueId(), target.getUniqueId());
                final StringBuilder msg = new StringBuilder("&7[&6SS&7]&r ");
                msg.append(playerPrefix);
                msg.append(p.getDisplayName());
                msg.append(this.c(" &7\u27a6 &3"));
                msg.append(targetPrefix);
                msg.append(target.getDisplayName());
                msg.append("&7: ");
                msg.append(sb.toString());
                for (final String sUUID : Config.socialSpy) {
                    final Player pl = Bukkit.getPlayer(UUID.fromString(sUUID));
                    if (pl != null && pl.isOnline()) {
                        MessageUtils.tell(pl, msg.toString());
                    }
                }
            }
        } else {
            p.sendMessage(this.c("�4You have no one to reply to."));
        }
    }

    public String c(final String input) {
        return MessageUtils.replaceColorCodes(input);
    }
}
