package me.crz.minecraft.kingdoms.chat.commands;

import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.List;

import org.bukkit.OfflinePlayer;

import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

import me.crz.minecraft.kingdoms.essentials.command.Command;

public class IgnoreCommand extends Command {
    private static Map<UUID, UUID> lastMessage;

    static {
        lastMessage = new HashMap<>();
    }

    public IgnoreCommand() {
        super("ignores a players chat and messages", "ignore");
        this.setUsage("ignore <list|player>");
    }

    public void run(final Player p, final String alias, final String[] args) {
        if (args.length < 1) {
            MessageUtils.tell(p, "&c" + this.getUsage());
            return;
        }
        if (args[0].equalsIgnoreCase("list")) {
            final Resident res = ResidentManager.getInstance().getResident(p);
            final StringBuilder sb = new StringBuilder("&7------- &cIgnored&7 -------\n&c");
            for (final String s : res.getIgnore()) {
                final OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(s));
                sb.append(String.valueOf(op.getName())).append("&7,&c");
            }
            MessageUtils.tell(p, sb.toString());
            return;
        }
        final Player target = getPlayer(args[0]);
        if (target != null) {
            final Resident res2 = ResidentManager.getInstance().getResident(p);
            if (res2.getIgnore().contains(target.getUniqueId().toString())) {
                res2.removeIgnore(target.getUniqueId().toString());
                ResidentManager.getInstance().saveResident(res2);
                MessageUtils.tell(p, "&7Player &6" + target.getName() + "&7 has been &aun-ignored&7!");
            } else {
                res2.addIgnore(target.getUniqueId().toString());
                ResidentManager.getInstance().saveResident(res2);
                MessageUtils.tell(p, "&7Player &6" + target.getName() + "&7 has been &cignored&7!");
            }
            return;
        }
        MessageUtils.tell(p, MessageUtils.PLAYER_NOT_FOUND_MESSAGE);
    }

    public static Player getPlayer(final String name) {
        try {
            return Bukkit.getPlayer(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static void setLastMessage(final UUID uuid1, final UUID uuid2) {
        lastMessage.put(uuid1, uuid2);
        lastMessage.put(uuid2, uuid1);
    }

    public static Player getLastMessage(final Player p) {
        if (lastMessage.containsKey(p.getUniqueId())) {
            return Bukkit.getPlayer(lastMessage.get(p.getUniqueId()));
        }
        return null;
    }
}
