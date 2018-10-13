package me.crz.minecraft.kingdoms.command;


import me.crz.minecraft.kingdoms.utils.ConfigKey;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class SubCommand implements ConfigKey {
    private SubCommand parent;
    private List<String> aliases;
    private String usage = "";
    private String permission = "";
    private boolean visible = true;
    private String name;

    private Map<String, SubCommand> subCommands = new LinkedHashMap<>();
    private boolean deepUsage = false;

    public SubCommand(SubCommand parent, String usage, String... aliases) {
        super();
        this.parent = parent;
        this.usage = usage;
        this.aliases = Arrays.stream(aliases).map(String::toLowerCase).collect(Collectors.toList());
        name = usage.split(" ")[0];
    }

    public boolean execute(Player player, String base, String alias, String... args) {
        if (!testAccess(player)) {
            return true;
        }
        if (args.length > 0) {
            SubCommand subCommand = matchSubCommand(args[0]);
            if (subCommand != null) {
                if (!subCommand.testAccess(player)) {
                    return true;
                }
                String path = base != null && !base.isEmpty() ? (base + " " + alias) : alias;
                if (!subCommand.execute(player, path, args[0].toLowerCase(), Arrays.copyOfRange(args, 1, args.length))) {
                    if (getMessage("usage").exists()) {
                        MessageUtils.tell(player, getMessage("usage"), "alias", path);
                        return true;
                    } else {
                        Collection<SubCommand> accessibleCommands = getAccessibleCommands(player);
                        if (!accessibleCommands.isEmpty()) {
                            MessageUtils.tell(player, Message.get("usage.head"),
                                    "name", StringUtils.capitalize(name()),
                                    "alias", StringUtils.capitalize(alias),
                                    "path", WordUtils.capitalize(path)
                            );
                            for (SubCommand s : accessibleCommands) {
                                MessageUtils.tell(player, Message.get("usage.entry"),
                                        "usage", hasUsage() ? s.getUsage(base) : "/" + path,
                                        "description", s.getDescription()
                                );
                            }
                        } else if (subCommand.hasUsage()) {
                            MessageUtils.tell(player, subCommand.getUsage(base));
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        return run(player, base, alias, args);
    }

    private SubCommand matchSubCommand(String input) {
        input = input.toLowerCase();
        if (!subCommands.containsKey(input)) {
            SubCommand subCommand = null;
            for (Map.Entry<String, SubCommand> s : subCommands.entrySet()) {
                if (s.getKey().startsWith(input)) {
                    if (subCommand == null) {
                        subCommand = s.getValue();
                    } else {
                        subCommands.put(input, null);
                        return null;
                    }
                }
            }
            subCommands.put(input, subCommand);
            return subCommand;
        }
        return subCommands.get(input);
    }

    private Collection<SubCommand> getAccessibleCommands(Player player) {
        Collection<SubCommand> accessibleCommands = new LinkedHashSet<>();
        for (SubCommand subCommand : subCommands.values()) {
            if (subCommand.hasAccess(player)) {
                if (subCommand.isDeepUsage()) {
                    accessibleCommands.addAll(subCommand.getAccessibleCommands(player));
                } else {
                    accessibleCommands.add(subCommand);
                }
            }
        }
        return accessibleCommands;
    }

    public boolean testAccess(Player player) {
        if (!hasAccess(player)) {
            MessageUtils.tell(player, getError("no-permission"), "permission", getPermission());
            return false;
        }
        return true;
    }

    public boolean hasAccess(Player player) {
        if (parent != null && !parent.hasAccess(player)) {
            return false;
        }
        return getPermission() == null || getPermission().isEmpty() || player.hasPermission(getPermission());
    }

    public abstract boolean run(Player p0, String p1, String p2, String... p3);

    public String name() {
        return name;
    }

    public String getKey() {
        return parent != null ? (parent.getKey() + "." + name()) : name();
    }

    public void setPermission(String perm) {
        permission = perm;
    }

    public String getPermission() {
        return permission;
    }

    public void setUsage(String... u) {
        usage = String.join("\n", u);
    }

    public void addAlias(String alias) {
        aliases.add(alias);
    }

    public void removeAlias(String alias) {
        aliases.remove(alias);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean hasUsage() {
        return usage != null && !usage.isEmpty();
    }

    public String getUsage(String base) {
        return "/" + base + " " + usage;
    }

    public String getDescription() {
        return getMessage("description").getText();
    }

    public List<String> getAliases() {
        return aliases;
    }

    public List<String> onTabComplete(Player p, String aliasUsed, String... args) {
        List<String> l = new ArrayList<>();
        if (args.length == 0) {
            return l;
        }
        String arg = args[args.length - 1];
        if (args.length == 1) {
            for (String subCommand : subCommands.keySet()) {
                if (subCommand.startsWith(arg.toLowerCase())) {
                    l.add(subCommand);
                }
            }
        }
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.getName().toLowerCase().startsWith(arg.toLowerCase())) {
                l.add(pl.getName());
            }
        }
        return l;
    }

    public SubCommand getParent() {
        return parent;
    }

    public Map<String, SubCommand> getSubCommands() {
        return subCommands;
    }

    protected String getText(String key) {
        return getMessage(key).getText();
    }

    protected Message getMessage(String key) {
        Message message = Message.get("commands." + getKey() + "." + key);
        return message.exists() ? message : Message.get(key);
    }

    protected Message getError(String key) {
        Message message = getMessage(key);
        return message.exists() ? message : getMessage("error." + key);
    }

    public void registerCommand(SubCommand c) {
        subCommands.put(c.name().toLowerCase(), c);
        for (String alias : c.getAliases()) {
            subCommands.putIfAbsent(alias.toLowerCase(), c);
        }
    }

    public void setDeepUsage(boolean deepUsage) {
        this.deepUsage = deepUsage;
    }

    private boolean isDeepUsage() {
        return deepUsage;
    }
}
