package me.crz.minecraft.kingdoms.command;


import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.rank.Rankable;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class SubCommand implements ConfigMessages, Rankable {
    private SubCommand parent;
    private List<String> aliases;
    private String usage;
    private String permission = "";
    private boolean visible = true;
    private String name;

    private Map<String, SubCommand> subCommands = new LinkedHashMap<>();
    private Map<String, SubCommand> subCommandAliases = new LinkedHashMap<>();
    private boolean forceOwnUsage = false;
    private HelpCommand helpCommand = null;

    public SubCommand(SubCommand parent, String usage, String... aliases) {
        super();
        this.parent = parent;
        this.usage = usage;
        this.aliases = Arrays.stream(aliases).map(String::toLowerCase).collect(Collectors.toList());
        name = usage.split(" ")[0];
    }

    public abstract boolean run(CommandSender sender, String base, String alias, String... args);

    public boolean execute(CommandSender sender, String base, String alias, String... args) {
        if (!testAccess(sender)) {
            return true;
        }
        if (args.length > 0) {
            String path = base != null && !base.isEmpty() ? (base + " " + alias) : alias;
            SubCommand subCommand = matchSubCommand(args[0]);
            if (subCommand != null) {
                if (!subCommand.testAccess(sender)) {
                    return true;
                }
                if (!subCommand.execute(sender, path, args[0].toLowerCase(), Arrays.copyOfRange(args, 1, args.length))) {
                    return subCommand.getHelpCommand().run(sender, path + " " + args[0], "help");
                }
                return true;
            }
        }
        return run(sender, base, alias, args);
    }

    public void setHelpCommand(HelpCommand helpCommand) {
        this.helpCommand = helpCommand;
        registerCommand(helpCommand);
    }

    public HelpCommand getHelpCommand() {
        if (helpCommand == null) {
            helpCommand = new HelpCommand(this);
        }
        return helpCommand;
    }

    protected boolean sendUsageMessage(CommandSender sender, String base, String alias) {
        String path = base != null && !base.isEmpty() ? (base + " " + alias) : alias;
        String coolPath = getPath();
        if (getMessage("usage").exists()) {
            getMessage("usage").tell(sender,
                    "alias", path,
                    "path", getPath(),
                    "usage", getUsage(base),
                    "aliases", getAliases().isEmpty() ? "" : getMessage("usage.aliases").getReplacedText("aliases", String.join(", ", getAliases())),
                    "description", getDescription()
            );
            return true;
        }

        boolean hasCommands = !getAccessibleCommands(sender).isEmpty();
        if (hasCommands) {
            getMessage("usage.head").tell(sender,
                    "usage", getUsage(base),
                    "path", getPath(),
                    "type", "All ",
                    "name", WordUtils.capitalizeFully(name()),
                    "alias", WordUtils.capitalizeFully(alias),
                    "aliases", getAliases().isEmpty() ? "" : getMessage("usage.aliases").getReplacedText("aliases", String.join(", ", getAliases())),
                    "path", WordUtils.capitalizeFully(path)
            );
        }
        boolean success = sendUsageList(sender, base, alias, path);
        if (hasCommands) {
            getMessage("usage.footer").tell(sender,
                    "usage", getUsage(base),
                    "path", getPath(),
                    "type", "All ",
                    "name", WordUtils.capitalizeFully(name()),
                    "alias", WordUtils.capitalizeFully(alias),
                    "aliases", getAliases().isEmpty() ? "" : getMessage("usage.aliases").getReplacedText("aliases", String.join(", ", getAliases())),
                    "path", WordUtils.capitalizeFully(path)
            );
        }
        return success;
    }

    private boolean sendUsageList(CommandSender sender, String base, String alias, String path) {
        Collection<SubCommand> accessibleCommands = getAccessibleCommands(sender);
        boolean success = false;
        if (isVisible() && (accessibleCommands.isEmpty() || isForceOwnUsage())) {
            getMessage("usage.entry").tell(sender,
                    "usage", getUsage(base),
                    "path", getPath(),
                    "aliases", getAliases().isEmpty() ? "" : getMessage("usage.aliases").getReplacedText("aliases", String.join(", ", getAliases())),
                    "description", getDescription()
            );
            success = true;
        }
        if (!accessibleCommands.isEmpty()) {
            accessibleCommands.stream().sorted(Rankable::compareTo).forEachOrdered(s -> s.sendUsageList(sender, path, s.name(), path + " " + s.name()));
            success = true;
        }
        return success;
    }

    public SubCommand matchSubCommand(String input) {
        input = input.toLowerCase();
        SubCommand subCommand = subCommands.get(input.toLowerCase());
        if (subCommand == null) {
            subCommand = subCommandAliases.get(input.toLowerCase());
            if (subCommand == null) {
                for (Map.Entry<String, SubCommand> s : subCommands.entrySet()) {
                    if (s.getKey().startsWith(input)) {
                        if (subCommand == null) {
                            subCommand = s.getValue();
                        }
                    }
                }
                if (subCommand != null) {
                    subCommandAliases.putIfAbsent(input, subCommand);
                }
            }
        }
        return subCommand;
    }

    public Collection<SubCommand> getAccessibleCommands(CommandSender sender) {
        Collection<SubCommand> accessibleCommands = new LinkedHashSet<>();
        for (SubCommand subCommand : subCommands.values()) {
            if (subCommand != null && subCommand.hasAccess(sender) && !(subCommand instanceof HelpCommand)) {
                accessibleCommands.add(subCommand);
            }
        }
        return accessibleCommands;
    }

    public boolean testAccess(CommandSender sender) {
        if (!hasAccess(sender)) {
            MessageUtils.tell(sender, getError("no-permission"), "permission", getPermission());
            return false;
        }
        return true;
    }

    public boolean hasAccess(CommandSender sender) {
        if (parent != null && !parent.hasAccess(sender)) {
            return false;
        }
        return getPermission() == null || getPermission().isEmpty() || sender.hasPermission(getPermission());
    }

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

    protected String getPath() {
        return parent != null ? parent.getPath() + " " + getShortestAlias(): getShortestAlias();
    }

    public void setPermission(String perm) {
        permission = perm;
    }

    public String getPermission() {
        if (permission == null || permission.isEmpty()) {
            if (getParent() != null) {
                permission = getParent().getPermission() + "." + name();
            } else {
                permission = "kingdoms.command." + name();
            }
        }
        return permission;
    }

    public void registerPermission() {
        Permission subPerm = Bukkit.getPluginManager().getPermission(getPermission());
        if (subPerm == null) {
            subPerm = new Permission(getPermission(), "Allows access to the command /" + getPath());
            Bukkit.getPluginManager().addPermission(subPerm);
        }
        if (getParent() != null) {
            subPerm.addParent(getParent().getPermission(), true);
        }
        getSubCommands().values().forEach(SubCommand::registerPermission);
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
            subCommandAliases.putIfAbsent(alias.toLowerCase(), c);
        }
    }

    public boolean isForceOwnUsage() {
        return forceOwnUsage;
    }

    public void setForceOwnUsage(boolean forceOwnUsage) {
        this.forceOwnUsage = forceOwnUsage;
    }

    public int ordinal() {
        return Integer.MIN_VALUE;
    }

    public String getShortestAlias() {
        String shortestAlias = name();
        for (String alias : getAliases()) {
            if (alias.length() < shortestAlias.length()) {
                shortestAlias = alias;
            }
        }
        return shortestAlias;
    }
}
