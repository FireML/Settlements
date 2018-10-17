package me.crz.minecraft.kingdoms.command;


import me.crz.minecraft.kingdoms.utils.ConfigMessages;
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

public abstract class SubCommand implements ConfigMessages {
    private SubCommand parent;
    private List<String> aliases;
    private String usage;
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
            String path = base != null && !base.isEmpty() ? (base + " " + alias) : alias;
            SubCommand subCommand = matchSubCommand(args[0]);
            if (subCommand != null) {
                if (!subCommand.testAccess(player)) {
                    return true;
                }
                if (!subCommand.execute(player, path, args[0].toLowerCase(), Arrays.copyOfRange(args, 1, args.length))) {
                    return subCommand.sendUsageMessage(player, path, args[0]);
                }
                return true;
            }
        }
        return run(player, base, alias, args);
    }

    protected boolean sendUsageMessage(Player player, String base, String alias) {
        String path = base != null && !base.isEmpty() ? (base + " " + alias) : alias;
        if (getMessage("usage").exists()) {
            MessageUtils.tell(player, getMessage("usage"), "alias", path, "usage", hasUsage() ? getUsage(base) : "/" + path);
            return true;
        }

        if (!getAccessibleCommands(player).isEmpty()) {
            MessageUtils.tell(player, Message.get("usage.head"),
                    "usage", hasUsage() ? getUsage(base) : "/" + path,
                    "name", StringUtils.capitalize(name()),
                    "alias", StringUtils.capitalize(alias),
                    "path", WordUtils.capitalize(path)
            );
        }
        return sendUsageList(player, base, alias, path);
    }

    private boolean sendUsageList(Player player, String base, String alias, String path) {
        Collection<SubCommand> accessibleCommands = getAccessibleCommands(player);
        if (!accessibleCommands.isEmpty()) {
            for (SubCommand s : accessibleCommands) {
                String subPath = path + " " + s.name();
                if (s.isDeepUsage()) {
                    s.sendUsageList(player, path, s.name(), subPath);
                } else {
                    MessageUtils.tell(player, Message.get("usage.entry"),
                            "usage", s.hasUsage() ? s.getUsage(path) : "/" + subPath,
                            "description", s.getDescription()
                    );
                }
            }
            return true;
        } else if (hasUsage()) {
            MessageUtils.tell(player, Message.get("usage.entry"),
                    "usage", hasUsage() ? getUsage(base) : "/" + path,
                    "description", getDescription()
            );
            return true;
        }
        return false;
    }

    private SubCommand matchSubCommand(String input) {
        input = input.toLowerCase();
        if (!subCommands.containsKey(input)) {
            SubCommand subCommand = null;
            for (Map.Entry<String, SubCommand> s : subCommands.entrySet()) {
                if (s.getKey().startsWith(input)) {
                    if (subCommand == null) {
                        subCommand = s.getValue();
                    }
                }
            }
            if (subCommand != null) {
                subCommands.put(input, subCommand);
            }
            return subCommand;
        }
        return subCommands.get(input);
    }

    public Collection<SubCommand> getAccessibleCommands(Player player) {
        Collection<SubCommand> accessibleCommands = new LinkedHashSet<>();
        for (SubCommand subCommand : subCommands.values()) {
            if (subCommand != null && subCommand.hasAccess(player)) {
                accessibleCommands.add(subCommand);
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

    @Override
    public String name() {
        return name;
    }

    @Override
    public String type() {
        return "command";
    }

    @Override
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
        return "/" + (base == null || base.isEmpty() ? "" : base + " ") + usage;
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
