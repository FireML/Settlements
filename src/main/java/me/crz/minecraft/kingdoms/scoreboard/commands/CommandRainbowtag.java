package me.crz.minecraft.kingdoms.scoreboard.commands;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.UUID;
import java.util.Set;
import me.crz.minecraft.kingdoms.essentials.command.Command;

public class CommandRainbowtag extends Command
{
    private static CommandRainbowtag instance;
    private Set<UUID> enabled;
    private String[] colours;
    
    public CommandRainbowtag() {
        super("Make your nametag flash like a rainbow!", new String[] { "rainbowtag" });
        enabled = new HashSet<UUID>();
        colours = new String[] { "a", "b", "c", "d", "e", "2", "3", "4", "5", "6" };
        instance = this;
    }
    
    public boolean isEnabled(final UUID uuid) {
        return enabled.contains(uuid);
    }
    
    public String[] getColours() {
        return colours;
    }
    
    public Set<UUID> getEnabled() {
        return enabled;
    }
    
    public static CommandRainbowtag getInstance() {
        return instance;
    }
    
    public void run(final Player p, final String alias, final String[] args) {
        if (enabled.contains(p.getUniqueId())) {
            enabled.remove(p.getUniqueId());
            p.sendMessage(MessageUtils.replaceColorCodes("Disabled rainbow tag."));
        }
        else {
            enabled.add(p.getUniqueId());
            p.sendMessage(MessageUtils.replaceColorCodes("&a&lEnabled rainbow tag."));
        }
    }
}
