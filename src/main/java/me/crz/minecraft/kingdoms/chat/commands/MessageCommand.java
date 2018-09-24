package me.crz.minecraft.kingdoms.chat.commands;

import java.util.Iterator;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.utils.config.Config;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
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

public class MessageCommand extends Command
{
    private static Map<UUID, UUID> lastMessage;
    
    static {
        lastMessage = new HashMap<UUID, UUID>();
    }
    
    public MessageCommand() {
        super("", new String[] { "message", "msg", "message", "tell", "pm", "m", "w", "t", "whisper", "emsg", "etell", "epm", "ewhisper" });
        this.setUsage(new String[] { "<name> <message>" });
    }
    
    public void run(final Player p, final String alias, final String[] args) {
        final StringBuilder sb = new StringBuilder();
        if (args.length < 2) {
            p.sendMessage(this.getUsage());
            return;
        }
        for (int i = 1; i < args.length; ++i) {
            sb.append(args[i]);
            sb.append(" ");
        }
        if (getPlayer(args[0]) == null) {
            p.sendMessage(String.valueOf(args[0]) + " isn't online");
            return;
        }
        final Player target = getPlayer(args[0]);
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
        final DecimalFormat df = new DecimalFormat("#,###.##");
        final String playerPrefix = KingdomsPlugin.chat.getPlayerPrefix(p);
        final String targetPrefix = KingdomsPlugin.chat.getPlayerPrefix(target);
        final TextComponent from = new TextComponent(this.c("&8["));
        from.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/r"));
        final TextComponent fromYou = new TextComponent(this.c("&ayou"));
        fromYou.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.c("&9Name: &7" + p.getDisplayName() + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(Bukkit.getOfflinePlayer(p.getUniqueId()))))).create()));
        from.addExtra((BaseComponent)fromYou);
        from.addExtra((BaseComponent)new TextComponent(this.c(" &7\u27a6 &3")));
        final TextComponent fromTo = new TextComponent(this.c(String.valueOf(targetPrefix) + target.getDisplayName()));
        fromTo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.c("&9Name: &7" + target.getDisplayName() + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(Bukkit.getOfflinePlayer(target.getUniqueId()))))).create()));
        from.addExtra((BaseComponent)fromTo);
        from.addExtra(this.c("&8] "));
        final TextComponent temp = new TextComponent(sb.toString());
        temp.setColor(ChatColor.AQUA);
        from.addExtra((BaseComponent)temp);
        final TextComponent to = new TextComponent(this.c("&8["));
        to.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/r"));
        final TextComponent toYou = new TextComponent(this.c("&ayou"));
        toYou.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.c("&9Name: &7" + target.getDisplayName() + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(Bukkit.getOfflinePlayer(target.getUniqueId()))))).create()));
        final TextComponent toTo = new TextComponent(this.c(String.valueOf(playerPrefix) + p.getDisplayName()));
        toTo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.c("&9Name: &7" + p.getDisplayName() + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(Bukkit.getOfflinePlayer(p.getUniqueId()))))).create()));
        to.addExtra((BaseComponent)toTo);
        to.addExtra((BaseComponent)new TextComponent(this.c(" &7\u27a6 &3")));
        to.addExtra((BaseComponent)toYou);
        to.addExtra(this.c("&8] "));
        to.addExtra((BaseComponent)temp);
        p.spigot().sendMessage((BaseComponent)from);
        target.spigot().sendMessage((BaseComponent)to);
        lastMessage.put(p.getUniqueId(), target.getUniqueId());
        lastMessage.put(target.getUniqueId(), p.getUniqueId());
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
    
    public String c(final String input) {
        return MessageUtils.replaceColorCodes(input);
    }
    
    public static Player getPlayer(final String name) {
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            if (!pl.isOnline()) {
                continue;
            }
            if (pl.getName().equalsIgnoreCase(name)) {
                return pl;
            }
        }
        return null;
    }
    
    public static void setLastMessage(final UUID uuid1, final UUID uuid2) {
        lastMessage.put(uuid1, uuid2);
        lastMessage.put(uuid2, uuid1);
    }
    
    public static Player getLastMessage(final Player p) {
        if (lastMessage.containsKey(p.getUniqueId())) {
            return Bukkit.getPlayer((UUID)lastMessage.get(p.getUniqueId()));
        }
        return null;
    }
}
