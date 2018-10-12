package me.crz.minecraft.kingdoms.chat.commands;

import java.util.stream.Collectors;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
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

    public void run(Player p, String alias, String[] args) {
        if (args.length < 1) {
            MessageUtils.tell(p, "&c" + this.getUsage());
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(p);
        Utils.runAsync(() -> {
            if (args[0].equalsIgnoreCase("list")) {
                MessageUtils.tell(p, "&7------- &cIgnored&7 -------\n&c"
                        + res.getIgnore().stream()
                        .map(id -> {
                            Resident resident = ResidentManager.getInstance().getResident(id);
                            if (resident != null) {
                                return resident.getName();
                            } else {
                                return id.toString();
                            }
                        }).collect(Collectors.joining("&7, &c"))
                );
                return;
            }
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target != null) {
                if (res.removeIgnore(target.getUUID())) {
                    MessageUtils.tell(p, "&7Player &6" + target.getName() + "&7 has been &aun-ignored&7!");
                } else {
                    res.addIgnore(target.getUUID());
                    MessageUtils.tell(p, "&7Player &6" + target.getName() + "&7 has been &cignored&7!");
                }
                Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
                return;
            }
            MessageUtils.tell(p, Message.get("player-not-found"), "player", args[0]);
        });
    }

    public static Player getPlayer(String name) {
        try {
            return Bukkit.getPlayer(name);
        } catch (Exception e) {
            return null;
        }
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
