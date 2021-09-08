package de.themoep.settlements.command.empire;

import com.google.common.collect.ImmutableMap;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class EmpireListCommand extends SubCommand {
    public EmpireListCommand(SubCommand parent) {
        super(parent, "list [open] [<page>]", "top");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        int page = 1;
        LandAccess access = null;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (IllegalArgumentException e1) {
                if ("open".equalsIgnoreCase(args[0])) {
                    access = LandAccess.OPEN;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (IllegalArgumentException e2) {
                            getError("not-a-number").tell(sender, "input", args[1]);
                            return false;
                        }
                    }
                } else {
                    getError("not-a-number").tell(sender, "input", args[0]);
                    return false;
                }
            }
        }
        if (page < 1) {
            getError("not-a-number").tell(sender, "input", String.valueOf(page));
            return false;
        }
        LandAccess finalAccess = access;
        int finalPage = page;
        Utils.runAsync(() -> {
            List<Empire> empires = EmpireManager.getInstance().getEmpires().stream()
                    .filter(v -> v.getAccess().atLeast(finalAccess))
                    .sorted(Empire::compareTo)
                    .collect(Collectors.toList());

            int pages = (int) Math.ceil(empires.size() / (double) Config.ENTRIES_PER_PAGE);
            getMessage("head").tell(sender,
                    "access", finalAccess != null ? finalAccess.getTag() + " " : "",
                    "page", String.valueOf(finalPage),
                    "maxpage", String.valueOf(pages)
            );
            for (int i = (finalPage - 1) * Config.ENTRIES_PER_PAGE; i < empires.size() && i < finalPage * Config.ENTRIES_PER_PAGE; i++) {
                Empire e = empires.get(i);
                getMessage("entry" + (e.isAbandoned() ? "-abandoned" : "")).tell(sender, e.getReplacements(this,
                        "place", String.valueOf(i + 1),
                        "access", e.getAccess() != LandAccess.PRIVATE ? e.getAccess().getTag() : ""
                ));
            }

            ComponentBuilder next = new ComponentBuilder("")
                    .append(getMessage("next").getReplacedComponents("next", String.valueOf(finalPage + 1)));
            if (finalPage + 1 > pages) {
                next.color(ChatColor.GRAY);
            } else {
                next.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + base + " " + alias + " " + (finalAccess != null ? finalAccess.name() + " " : "") + (finalPage + 1)));
                next.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(ChatColor.YELLOW + ">>"))));
            }
            ComponentBuilder previous = new ComponentBuilder("")
                    .append(getMessage("previous").getReplacedComponents("previous", String.valueOf(finalPage - 1)));
            if (finalPage - 1 <= 0) {
                previous.color(ChatColor.GRAY);
            } else {
                previous.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + base + " " + alias + " " + (finalAccess != null ? finalAccess.name() + " " : "") + (finalPage - 1)));
                previous.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(ChatColor.YELLOW + "<<"))));
            }

            MessageUtils.tell(sender, MessageUtils.replacePlaceholders(getMessage("footer").getReplacedComponents(
                    "access", finalAccess != null ? finalAccess.getTag() + " " : "",
                    "page", String.valueOf(finalPage),
                    "maxpage", String.valueOf(pages)
            ), ImmutableMap.of(
                    "next", next.create(),
                    "previous", previous.create()
            )));
        });

        return true;
    }
}
