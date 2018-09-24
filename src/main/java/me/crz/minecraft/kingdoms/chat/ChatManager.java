package me.crz.minecraft.kingdoms.chat;

import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.village.Village;
import java.util.Iterator;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import net.md_5.bungee.api.chat.BaseComponent;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.List;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import java.text.DecimalFormat;
import org.bukkit.event.player.PlayerChatEvent;
import me.crz.minecraft.kingdoms.Base;

public class ChatManager extends Base
{
    private static ChatManager instance;
    
    public ChatManager() {
        super();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(final PlayerChatEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final DecimalFormat df = new DecimalFormat("#,###.##");
        final Resident res = ResidentManager.getInstance().getResident(e.getPlayer());
        final String playerPrefix = KingdomsPlugin.chat.getPlayerPrefix(e.getPlayer());
        if (res.getActiveChannel().equals(ChatChannel.GENERAL)) {
            e.setCancelled(true);
            if (Config.muteChat && !e.getPlayer().hasPermission("kingdoms.staff")) {
                MessageUtils.tell(e.getPlayer(), "&c&lChat is currently locked!");
                return;
            }
            if (e.getPlayer().hasPermission("kingdoms.chat.colour")) {
                e.setMessage(MessageUtils.replaceColorCodes(e.getMessage()));
            }
            final StringBuilder chat = new StringBuilder();
            chat.append(MessageUtils.replaceColorCodes(playerPrefix));
            chat.append(MessageUtils.replaceColorCodes("&7"));
            chat.append(e.getPlayer().getDisplayName());
            chat.append(MessageUtils.replaceColorCodes(" &7\u27a6&r "));
            final TextComponent nameBit = new TextComponent(chat.toString());
            nameBit.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + e.getPlayer().getName() + " "));
            nameBit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MessageUtils.replaceColorCodes("&9Name: &7" + e.getPlayer().getDisplayName() + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(Bukkit.getOfflinePlayer(e.getPlayer().getUniqueId()))))).create()));
            final TextComponent send;
            final BaseComponent baseComponent;
            final TextComponent message;
            ((List<Object>)Bukkit.getOnlinePlayers().stream().filter(p -> !ResidentManager.getInstance().getResident(p).getIgnore().contains(e.getPlayer().getUniqueId().toString())).filter(p -> Base.getResidentManager().isListening(p, ChatChannel.GENERAL)).<List<Object>, ?>collect((Collector<? super Object, ?, List<Object>>)Collectors.<? super Object>toList())).forEach(p -> {
                send = new TextComponent();
                send.addExtra(baseComponent);
                message = new TextComponent();
                if (!ResidentManager.getInstance().getResident(p).getSwear()) {
                    message.addExtra((BaseComponent)new TextComponent(Base.getPlugin().utilChat.filterCurses(e.getMessage())));
                }
                else {
                    message.addExtra((BaseComponent)new TextComponent(e.getMessage()));
                }
                send.addExtra((BaseComponent)message);
                p.spigot().sendMessage((BaseComponent)send);
                return;
            });
            System.out.println(MessageUtils.replaceColorCodes(String.valueOf(chat.toString()) + e.getMessage()));
        }
        else {
            final Player p2 = e.getPlayer();
            e.setCancelled(true);
            if (res.getActiveChannel().equals(ChatChannel.LOCAL)) {
                final StringBuilder sb = new StringBuilder("&e[Local] &7");
                sb.append(String.valueOf(playerPrefix) + p2.getDisplayName());
                sb.append(" \u27a6 &e");
                sb.append(e.getMessage());
                MessageUtils.tell(p2, sb.toString());
                System.out.println(MessageUtils.replaceColorCodes(sb.toString()));
                for (final Entity en : p2.getNearbyEntities(200.0, 200.0, 200.0)) {
                    if (en instanceof Player && Base.getResidentManager().isListening((Player)en, ChatChannel.LOCAL)) {
                        MessageUtils.tell((Player)en, sb.toString());
                    }
                }
                return;
            }
            if (res.getActiveChannel().equals(ChatChannel.ADMIN)) {
                final StringBuilder sb = new StringBuilder("&b[Admin] &7");
                sb.append(p2.getDisplayName());
                sb.append(" \u27a6&r&5 ");
                sb.append(e.getMessage());
                System.out.println(MessageUtils.replaceColorCodes(sb.toString()));
                for (final Player op : Bukkit.getOnlinePlayers()) {
                    if (op.hasPermission("village.admin") && Base.getResidentManager().isListening(op, ChatChannel.ADMIN)) {
                        MessageUtils.tell(op, sb.toString());
                    }
                }
                return;
            }
            if (res.getActiveChannel().equals(ChatChannel.STAFF)) {
                final StringBuilder sb = new StringBuilder("&9[Staff] &7");
                sb.append(p2.getDisplayName());
                sb.append(" \u27a6&r&6 ");
                sb.append(e.getMessage());
                System.out.println(MessageUtils.replaceColorCodes(sb.toString()));
                for (final Player op : Bukkit.getOnlinePlayers()) {
                    if (op.hasPermission("village.staff") && Base.getResidentManager().isListening(op, ChatChannel.STAFF)) {
                        MessageUtils.tell(op, sb.toString());
                    }
                }
                return;
            }
            if (res.getActiveChannel().equals(ChatChannel.OFFICER)) {
                final Village village = VillageManager.getInstance().getVillageFromPlayer(p2);
                if (village == null) {
                    res.setActiveChannel(ChatChannel.GENERAL);
                    ResidentManager.getInstance().saveResident(res);
                    this.on(e);
                    return;
                }
                final StringBuilder sb2 = new StringBuilder("&e[Officer] &7");
                sb2.append(p2.getDisplayName());
                sb2.append("&r \u27a6 ");
                sb2.append(e.getMessage());
                System.out.println(MessageUtils.replaceColorCodes(sb2.toString()));
                for (final Resident resident : ResidentManager.getInstance().getResidentsForVillage(village)) {
                    final Player op2 = Bukkit.getPlayer(resident.getUUID());
                    if (op2 == null) {
                        continue;
                    }
                    if ((!Utils.playerIsChief(op2, VillageManager.getInstance().getVillageFromPlayer(op2)) && !Utils.playerIsElder(op2)) || !Base.getResidentManager().isListening(op2, ChatChannel.OFFICER)) {
                        continue;
                    }
                    MessageUtils.tell(op2, sb2.toString());
                }
            }
            else if (res.getActiveChannel().equals(ChatChannel.SETTLEMENT)) {
                final Village village = VillageManager.getInstance().getVillageFromPlayer(p2);
                if (village == null) {
                    res.setActiveChannel(ChatChannel.GENERAL);
                    ResidentManager.getInstance().saveResident(res);
                    this.on(e);
                    return;
                }
                final StringBuilder sb2 = new StringBuilder("&b[");
                sb2.append(village.getState().name().charAt(0));
                sb2.append(village.getState().name().toLowerCase().substring(1));
                sb2.append("] &7");
                sb2.append(p2.getDisplayName());
                sb2.append(" \u27a6 &a");
                sb2.append(e.getMessage());
                System.out.println(MessageUtils.replaceColorCodes(sb2.toString()));
                MessageUtils.tellResidentsC(village.getResidents(), sb2.toString(), ChatChannel.SETTLEMENT);
            }
            else {
                if (res.getActiveChannel().equals(ChatChannel.TRADE)) {
                    final StringBuilder sb = new StringBuilder("&d[TRADE] &r");
                    sb.append(p2.getDisplayName());
                    sb.append(" \u27a6 &f");
                    sb.append(e.getMessage());
                    System.out.println(MessageUtils.replaceColorCodes(sb.toString()));
                    for (final Player op : Bukkit.getOnlinePlayers()) {
                        if (Base.getResidentManager().isListening(op, ChatChannel.TRADE)) {
                            MessageUtils.tell(op, sb.toString());
                        }
                    }
                    return;
                }
                if (res.getActiveChannel().equals(ChatChannel.ALLY)) {
                    final Village village = VillageManager.getInstance().getVillageFromPlayer(p2);
                    if (village == null) {
                        res.setActiveChannel(ChatChannel.GENERAL);
                        ResidentManager.getInstance().saveResident(res);
                        this.on(e);
                        return;
                    }
                    final StringBuilder sb2 = new StringBuilder("&b[Ally] &6");
                    sb2.append(village.getState().name().charAt(0));
                    sb2.append(village.getState().name().toLowerCase().substring(1));
                    sb2.append(" ");
                    sb2.append(p2.getDisplayName());
                    sb2.append(" \u27a6 ");
                    sb2.append(e.getMessage());
                    System.out.println(MessageUtils.replaceColorCodes(sb2.toString()));
                    MessageUtils.tellResidentsC(village.getResidents(), sb2.toString(), ChatChannel.SETTLEMENT);
                    for (final String s : village.getAllies()) {
                        final Village tVillage = VillageManager.getInstance().getVillageFromName(s);
                        if (tVillage == null) {
                            continue;
                        }
                        MessageUtils.tellResidentsC(tVillage.getResidents(), sb2.toString(), ChatChannel.ALLY);
                    }
                }
            }
        }
    }
    
    public static ChatManager getInstance() {
        if (instance == null) {
            Base.registerListener((Listener)(instance = new ChatManager()));
        }
        return instance;
    }
    
    private static /* synthetic */ boolean lambda$0(final PlayerChatEvent playerChatEvent, final Player p) {
        return !ResidentManager.getInstance().getResident(p).getIgnore().contains(playerChatEvent.getPlayer().getUniqueId().toString());
    }
    
    private static /* synthetic */ boolean lambda$1(final Player p) {
        return Base.getResidentManager().isListening(p, ChatChannel.GENERAL);
    }
    
    private static /* synthetic */ void lambda$2(final TextComponent textComponent, final PlayerChatEvent playerChatEvent, final Player p) {
        final TextComponent send = new TextComponent();
        send.addExtra((BaseComponent)textComponent);
        final TextComponent message = new TextComponent();
        if (!ResidentManager.getInstance().getResident(p).getSwear()) {
            message.addExtra((BaseComponent)new TextComponent(Base.getPlugin().utilChat.filterCurses(playerChatEvent.getMessage())));
        }
        else {
            message.addExtra((BaseComponent)new TextComponent(playerChatEvent.getMessage()));
        }
        send.addExtra((BaseComponent)message);
        p.spigot().sendMessage((BaseComponent)send);
    }
}
