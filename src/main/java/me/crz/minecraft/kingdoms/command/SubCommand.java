package me.crz.minecraft.kingdoms.command;

import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.ListIterator;
import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import me.crz.minecraft.kingdoms.Base;

public abstract class SubCommand extends Base
{
    private String description;
    private List<String> aliases;
    private String usage;
    private String permission;
    private boolean visible;
    private boolean console;
    private String baseAlias;
    
    public SubCommand(final String description, final String... aliases) {
        super();
        aliases = new ArrayList<String>();
        usage = "";
        permission = "";
        visible = true;
        console = false;
        baseAlias = "";
        description = description;
        aliases = new ArrayList<String>(Arrays.<String>asList(aliases));
        baseAlias = aliases.get(0);
        final ListIterator<String> iterator = aliases.listIterator();
        while (iterator.hasNext()) {
            iterator.set(((String)iterator.next()).toLowerCase());
        }
    }
    
    public abstract void run(final Player p0, final String p1, final String p2, final String... p3);
    
    public String getBaseAlias() {
        return baseAlias;
    }
    
    public void setPermission(final String perm) {
        permission = perm;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public void setUsage(final String... u) {
        usage = String.join(" ", (CharSequence[])u);
    }
    
    public boolean isConsoleCompat() {
        return console;
    }
    
    public void setConsoleCompat(final Boolean console) {
        this.console = console;
    }
    
    public void addAlias(final String alias) {
        aliases.add(alias);
    }
    
    public void removeAlias(final String alias) {
        if (aliases.contains(alias)) {
            aliases.remove(alias);
        }
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }
    
    public String getUsage(final String base) {
        return ChatColor.RED + "/" + base + " " + usage;
    }
    
    public String getDescription() {
        return description;
    }
    
    public List<String> getAliases() {
        return aliases;
    }
    
    public void couldNotFind(final Player p, final String what, final String thing) {
        p.sendMessage("Could not find " + what + "�7 '�e" + thing + "�7'.");
    }
    
    public List<String> onTabComplete(final Player p, final String aliasUsed, final String... args) {
        final List<String> l = new ArrayList<String>();
        if (args.length == 0) {
            return l;
        }
        final String arg = args[args.length - 1];
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            if (!l.contains(pl.getName()) && pl.getName().toLowerCase().startsWith(arg.toLowerCase())) {
                l.add(pl.getName());
            }
        }
        return l;
    }
}
