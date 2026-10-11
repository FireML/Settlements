package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.settlement.JoinType;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import de.themoep.settlements.utils.ComponentUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class EmpireListCommand extends SubCommand {
    public EmpireListCommand(SubCommand parent) {
        super(parent, "list [open] [<page>]", "top");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        int page = 1;
        String tag = null;
        Predicate<Empire> filter = e -> true;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (IllegalArgumentException e1) {
                if ("open".equalsIgnoreCase(args[0])) {
                    filter = e -> e.getJoinType() == JoinType.ANYONE;
                    tag = "open";
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
        int finalPage = page;
        Predicate<Empire> finalFilter = filter;
        String finalTag = tag;
        Utils.runAsync(() -> {
            List<Empire> empires = EmpireManager.getInstance().getEmpires().stream()
                    .filter(finalFilter)
                    .sorted(Empire::compareTo)
                    .toList();

            int pages = (int) Math.ceil(empires.size() / (double) Config.ENTRIES_PER_PAGE);
            getMessage("head").tell(sender,
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

            Component next = getMessage("next").getReplacedComponent("next", String.valueOf(finalPage + 1));
            if (finalPage + 1 > pages) {
                next = next.color(NamedTextColor.GRAY);
            } else {
                next = next.color(NamedTextColor.AQUA);
                next = next.clickEvent(ClickEvent.runCommand("/" + base + " " + alias + " " + (finalTag != null ? finalTag + " " : "") + (finalPage + 1)));
                next = next.hoverEvent(HoverEvent.showText(ComponentUtils.parseComponent("&e>>")));
            }
            Component previous = getMessage("previous").getReplacedComponent("previous", String.valueOf(finalPage - 1));
            if (finalPage - 1 <= 0) {
                previous = previous.color(NamedTextColor.GRAY);
            } else {
                previous = previous.color(NamedTextColor.AQUA);
                previous = previous.clickEvent(ClickEvent.runCommand("/" + base + " " + alias + " " + (finalTag != null ? finalTag + " " : "") + (finalPage - 1)));
                previous = previous.hoverEvent(HoverEvent.showText(ComponentUtils.parseComponent("&e<<")));
            }

            Map<String, Component> replacements = Map.of(
                    "page", ComponentUtils.parseComponent(String.valueOf(finalPage)),
                    "maxpage", ComponentUtils.parseComponent(String.valueOf(pages)),
                    "next", next,
                    "previous", previous.append(Component.text("").color(null))
            );

            MessageUtils.tell(sender, ComponentUtils.parsePlaceholders(getMessage("footer").getComponent(), replacements));
        });

        return true;
    }
}
