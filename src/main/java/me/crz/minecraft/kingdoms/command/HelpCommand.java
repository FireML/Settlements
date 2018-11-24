package me.crz.minecraft.kingdoms.command;

import com.google.common.collect.ImmutableMap;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class HelpCommand extends SubCommand {
    private Function<CommandSender, String> defaultTypeProvider;

    public HelpCommand(SubCommand parent) {
        super(parent, "help [<rank>] [<page>]", "usage");
        setVisible(false);
        if (parent == null) {
            throw new IllegalArgumentException("Parent sub command cannot be null!");
        }
        registerCommand(new SubCommand(this, "all") {
            @Override
            public boolean run(CommandSender sender, String base, String alias, String... args) {
                base = base.substring(0, base.lastIndexOf(' '));
                return parent.sendUsageMessage(sender, base.contains(" ") ? base.substring(0, base.lastIndexOf(' ')) : null, base.contains(" ") ? base.substring(base.lastIndexOf(' ')) : base);
            }
        });
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (base == null) {
            base = "";
        }
        int page = 1;
        int typeIndex = -1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[args.length - 1]);
                typeIndex = args.length - 2;
            } catch (NumberFormatException e) {
                typeIndex = 0;
            }
        }

        String defaultType = defaultTypeProvider != null ? defaultTypeProvider.apply(sender) : "general";
        String type = defaultType;
        String key = getParent().getKey();
        if (typeIndex >= 0 && args.length > typeIndex) {
            type = args[typeIndex];
            if (!ConfigManager.getInstance().getCommandsConfig().isList("help-order." + key + "." + type)) {
                type = defaultType;
            }
        }

        if (!ConfigManager.getInstance().getCommandsConfig().isList("help-order." + key + "." + type)) {
            type = "general";
        }

        boolean direct = false;
        String path = "help-order." + key + "." + type;
        if (!ConfigManager.getInstance().getCommandsConfig().isList(path)) {
            path = "help-order." + key;
            direct = true;
        }

        if (ConfigManager.getInstance().getCommandsConfig().isList(path)) {
            Map<String, SubCommand> commands = new LinkedHashMap<>();

            for (String commandPath : ConfigManager.getInstance().getCommandsConfig().getStringList(path)) {
                SubCommand subCommand = null;
                for (String part : stripParameters(commandPath).split(" ")) {
                    if (subCommand == null) {
                        subCommand = getParent().getSubCommands().get(part.toLowerCase());
                        if (subCommand == null) {
                            subCommand = CommandManager.getInstance().getCommand(part);
                        }
                    } else {
                        subCommand = subCommand.matchSubCommand(part);
                    }
                }
                if (subCommand == null || subCommand.hasAccess(sender)) {
                    commands.put(commandPath, subCommand);
                }
            }

            if (!commands.isEmpty()) {
                int entriesPerPage = Utils.getEntriesPerPage();
                int maxPage = (int) Math.ceil(commands.size() / (double) entriesPerPage);
                if (page > maxPage) {
                    page = maxPage;
                }
                getParent().getMessage("usage.head").tell(sender,
                        "type", direct ? "" : WordUtils.capitalizeFully(type) + " ",
                        "usage", getParent().getUsage(base.contains(" ") ? base.substring(0, base.lastIndexOf(' ')) : ""),
                        "name", WordUtils.capitalizeFully(getParent().name()),
                        "aliases", getParent().getAliases().isEmpty() ? "" : getParent().getMessage("usage.aliases").getReplacedText("aliases", String.join(", ", getParent().getAliases())),
                        "path", WordUtils.capitalizeFully(base)
                );
                commands.entrySet().stream().skip((page - 1) * entriesPerPage).limit(entriesPerPage).forEachOrdered(e -> {
                    SubCommand s = e.getValue();
                    String usage;
                    String displayPath;
                    String description;
                    List<String> aliases = new ArrayList<>();
                    if (s == null) {
                        usage = "/" + e.getKey();
                        displayPath = stripParameters(e.getKey());
                        String configPath = displayPath.replace(' ', '.');
                        Message message = Message.get("command." + configPath + ".description");
                        if (message.exists()) {
                            description = message.getText();
                        } else {
                            Message m = Message.get("command." + configPath);
                            if (m.exists()) {
                                description = m.getText();
                            } else {
                                description = message.getText();
                            }
                        }
                    } else {
                        displayPath = s.getPath();
                        if (!e.getKey().toLowerCase().startsWith(s.name())) {
                            SubCommand startCommand = CommandManager.getInstance().getCommand(e.getKey().split(" ")[0]);
                            if (startCommand != null && startCommand.getParent() == null) {
                                displayPath = stripParameters(e.getKey());
                            }
                        }
                        usage = "/" + displayPath;
                        int arrowIndex = e.getKey().indexOf('<');
                        int bracketIndex = e.getKey().indexOf('[');
                        if (arrowIndex > 0 && (bracketIndex < 0 || arrowIndex < bracketIndex)) {
                            usage += " " + e.getKey().substring(arrowIndex);
                        } else if (bracketIndex > 0 && (arrowIndex < 0 || bracketIndex < arrowIndex)) {
                            usage += " " + e.getKey().substring(bracketIndex);
                        }
                        description = s.getDescription();
                        aliases.addAll(s.getAliases());
                    }
                    getParent().getMessage("usage.entry").tell(sender,
                            "usage", usage,
                            "path", displayPath,
                            "aliases", aliases.isEmpty() ? "" : getParent().getMessage("usage.aliases").getReplacedText("aliases", String.join(", ", aliases)),
                            "description",description
                    );
                });
                getParent().getMessage("usage.footer").tell(sender,
                        "type", direct ? "" : WordUtils.capitalizeFully(type) + " ",
                        "usage", getParent().getUsage(base.contains(" ") ? base.substring(0, base.lastIndexOf(' ')) : ""),
                        "name", WordUtils.capitalizeFully(getParent().name()),
                        "aliases", getParent().getAliases().isEmpty() ? "" : getParent().getMessage("usage.aliases").getReplacedText("aliases", String.join(", ", getParent().getAliases())),
                        "path", WordUtils.capitalizeFully(base)
                );
                if (maxPage > 1) {
                    ComponentBuilder next = new ComponentBuilder("")
                            .append(getParent().getMessage("usage.pagination.next").getReplacedComponents("next", String.valueOf(page + 1)));
                    if (page + 1 > maxPage) {
                        next.color(net.md_5.bungee.api.ChatColor.GRAY);
                    } else {
                        next.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + (base.isEmpty() ? "" : base + " ") + alias + " " + type + " " + (page + 1)));
                        next.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(net.md_5.bungee.api.ChatColor.YELLOW + ">>")));
                    }
                    ComponentBuilder previous = new ComponentBuilder("")
                            .append(getParent().getMessage("usage.pagination.previous").getReplacedComponents("previous", String.valueOf(page - 1)));
                    if (page - 1 <= 0) {
                        previous.color(net.md_5.bungee.api.ChatColor.GRAY);
                    } else {
                        previous.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + (base.isEmpty() ? "" : base + " ") + alias + " " + type + " " + (page - 1)));
                        previous.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(net.md_5.bungee.api.ChatColor.YELLOW + "<<")));
                    }

                    MessageUtils.tell(sender, MessageUtils.replacePlaceholders(getParent().getMessage("usage.pagination.footer").getReplacedComponents(
                            "type", WordUtils.capitalizeFully(type),
                            "page", String.valueOf(page),
                            "maxpage", String.valueOf(maxPage)
                    ), ImmutableMap.of(
                            "next", next.create(),
                            "previous", previous.create()
                    )));
                }
                return true;
            }
        }

        int helpIndex = base.lastIndexOf(" help");
        base = helpIndex > -1 ? base.substring(0, helpIndex) : base;
        int spaceIndex = base.lastIndexOf(' ');
        return getParent().sendUsageMessage(sender, spaceIndex > -1 ? base.substring(0, spaceIndex) : "", spaceIndex > -1 ? base.substring(spaceIndex) : base);
    }

    private String stripParameters(String string) {
        String command = string;
        if (command.contains("<")) {
            command = command.substring(0, command.indexOf('<'));
        }
        if (command.contains("[")) {
            command = command.substring(0, command.indexOf('['));
        }
        command = command.trim();
        return command.isEmpty() ? string : command;
    }

    public void setDefaultTypeProvider(Function<CommandSender, String> typeProvider) {
        this.defaultTypeProvider = typeProvider;
    }
}
