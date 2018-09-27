package me.crz.minecraft.kingdoms.chat;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.village.Village;

import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.UUID;

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

import org.bukkit.event.player.AsyncPlayerChatEvent;
import me.crz.minecraft.kingdoms.Base;

public class ChatManager extends Base {
    private static ChatManager instance;

    public ChatManager() {
        super();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) {
            return;
        }
        DecimalFormat df = new DecimalFormat("#,###.##");
        Resident res = ResidentManager.getInstance().getResident(e.getPlayer());
        String playerPrefix = KingdomsPlugin.chat.getPlayerPrefix(e.getPlayer());

        Player player = e.getPlayer();
        e.setCancelled(true);
        Village village;
        StringBuilder sb;
        if (e.getPlayer().hasPermission("kingdoms.chat.colour")) {
            e.setMessage(MessageUtils.replaceColorCodes(e.getMessage()));
        }
        switch (res.getActiveChannel()) {
            case GENERAL:
                if (Config.muteChat && !e.getPlayer().hasPermission("kingdoms.staff")) {
                    MessageUtils.tell(e.getPlayer(), "&c&lChat is currently locked!");
                    return;
                }
                BaseComponent[] nameBit = new ComponentBuilder("")
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + e.getPlayer().getName() + " "))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MessageUtils.replaceColorCodes("&9Name: &7" + e.getPlayer().getDisplayName() + "\n&9Balance: &7" + df.format(KingdomsPlugin.getInstance().econ.getBalance(Bukkit.getOfflinePlayer(e.getPlayer().getUniqueId()))))).create()))
                        .append(TextComponent.fromLegacyText(MessageUtils.replaceColorCodes(playerPrefix)))
                        .append(e.getPlayer().getDisplayName())
                        .color(ChatColor.GRAY)
                        .append(" \u27a6 ")
                        .color(ChatColor.GRAY)
                        .create();

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!ResidentManager.getInstance().getResident(p).getIgnore().contains(e.getPlayer().getUniqueId())
                            && Base.getResidentManager().isListening(p, ChatChannel.GENERAL)) {
                        ComponentBuilder send = new ComponentBuilder("")
                                .append(nameBit);
                        if (!ResidentManager.getInstance().getResident(p).getSwear()) {
                            send.append(TextComponent.fromLegacyText(Base.getPlugin().utilChat.filterCurses(e.getMessage())), ComponentBuilder.FormatRetention.NONE);
                        } else {
                            send.append(TextComponent.fromLegacyText(e.getMessage()), ComponentBuilder.FormatRetention.NONE);
                        }
                        p.spigot().sendMessage(send.create());
                    }
                }
                System.out.println(TextComponent.toLegacyText(nameBit) + e.getMessage());

            case LOCAL:
                sb = new StringBuilder("&e[Local] &7");
                sb.append(String.valueOf(playerPrefix)).append(player.getDisplayName());
                sb.append(" \u27a6 &e");
                sb.append(e.getMessage());
                MessageUtils.tell(player, sb.toString());
                System.out.println(MessageUtils.replaceColorCodes(sb.toString()));
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (player.getLocation().getWorld().equals(p.getWorld())
                            && player.getLocation().distanceSquared(p.getLocation()) < 200 * 200
                            && ResidentManager.getInstance().isListening(p, ChatChannel.LOCAL)) {
                        MessageUtils.tell(p, sb.toString());
                    }
                }
                return;
            case ADMIN:
                sb = new StringBuilder("&b[Admin] &7");
                sb.append(player.getDisplayName());
                sb.append(" \u27a6&r&5 ");
                sb.append(e.getMessage());
                System.out.println(MessageUtils.replaceColorCodes(sb.toString()));
                for (Player op : Bukkit.getOnlinePlayers()) {
                    if (op.hasPermission("village.admin") && Base.getResidentManager().isListening(op, ChatChannel.ADMIN)) {
                        MessageUtils.tell(op, sb.toString());
                    }
                }
                return;
            case STAFF:
                sb = new StringBuilder("&9[Staff] &7");
                sb.append(player.getDisplayName());
                sb.append(" \u27a6&r&6 ");
                sb.append(e.getMessage());
                System.out.println(MessageUtils.replaceColorCodes(sb.toString()));
                for (Player op : Bukkit.getOnlinePlayers()) {
                    if (op.hasPermission("village.staff") && Base.getResidentManager().isListening(op, ChatChannel.STAFF)) {
                        MessageUtils.tell(op, sb.toString());
                    }
                }
                return;
            case OFFICER:
                village = VillageManager.getInstance().getVillageFromPlayer(player);
                if (village == null) {
                    if (res.setActiveChannel(ChatChannel.GENERAL)) {
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
                    }
                    this.on(e);
                    return;
                }
                sb = new StringBuilder("&e[Officer] &7");
                sb.append(player.getDisplayName());
                sb.append("&r \u27a6 ");
                sb.append(e.getMessage());
                System.out.println(MessageUtils.replaceColorCodes(sb.toString()));
                for (UUID residentId : village.getResidentIds()) {
                    Player op2 = Bukkit.getPlayer(residentId);
                    if (op2 == null) {
                        continue;
                    }
                    if ((!Utils.playerIsChief(op2, VillageManager.getInstance().getVillageFromPlayer(op2)) && !Utils.playerIsElder(op2)) || !Base.getResidentManager().isListening(op2, ChatChannel.OFFICER)) {
                        continue;
                    }
                    MessageUtils.tell(op2, sb.toString());
                }
                return;
            case SETTLEMENT:
                village = VillageManager.getInstance().getVillageFromPlayer(player);
                if (village == null) {
                    if (res.setActiveChannel(ChatChannel.GENERAL)) {
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
                    }
                    this.on(e);
                    return;
                }
                sb = new StringBuilder("&b[");
                sb.append(village.getState().name().charAt(0));
                sb.append(village.getState().name().toLowerCase().substring(1));
                sb.append("] &7");
                sb.append(player.getDisplayName());
                sb.append(" \u27a6 &a");
                sb.append(e.getMessage());
                System.out.println(MessageUtils.replaceColorCodes(sb.toString()));
                MessageUtils.tellResidentsC(village.getOnlineResidents(), sb.toString(), ChatChannel.SETTLEMENT);
            case TRADE:
                sb = new StringBuilder("&d[TRADE] &r");
                sb.append(player.getDisplayName());
                sb.append(" \u27a6 &f");
                sb.append(e.getMessage());
                System.out.println(MessageUtils.replaceColorCodes(sb.toString()));
                for (Player op : Bukkit.getOnlinePlayers()) {
                    if (Base.getResidentManager().isListening(op, ChatChannel.TRADE)) {
                        MessageUtils.tell(op, sb.toString());
                    }
                }
                return;
            case ALLY:
                village = VillageManager.getInstance().getVillageFromPlayer(player);
                if (village == null) {
                    if (res.setActiveChannel(ChatChannel.GENERAL)) {
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
                    }
                    this.on(e);
                    return;
                }
                sb = new StringBuilder("&b[Ally] &6");
                sb.append(village.getState().name().charAt(0));
                sb.append(village.getState().name().toLowerCase().substring(1));
                sb.append(" ");
                sb.append(player.getDisplayName());
                sb.append(" \u27a6 ");
                sb.append(e.getMessage());
                System.out.println(MessageUtils.replaceColorCodes(sb.toString()));
                MessageUtils.tellResidentsC(village.getResidents(), sb.toString(), ChatChannel.SETTLEMENT);
                for (String s : village.getAllies()) {
                    Village tVillage = VillageManager.getInstance().getVillageFromName(s);
                    if (tVillage == null) {
                        continue;
                    }
                    MessageUtils.tellResidentsC(tVillage.getResidents(), sb.toString(), ChatChannel.ALLY);
                }
                return;
        }
    }

    public static ChatManager getInstance() {
        if (instance == null) {
            Base.registerListener(instance = new ChatManager());
        }
        return instance;
    }
}
