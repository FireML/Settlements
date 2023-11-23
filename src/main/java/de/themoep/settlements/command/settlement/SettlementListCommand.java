package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.settlement.JoinType;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import uk.firedev.poleislib.utils.ComponentUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class SettlementListCommand extends SubCommand {
    public SettlementListCommand(SubCommand parent) {
        super(parent, "list [open] [<page>]", "top");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        int page = 1;
        String tag = null;
        Predicate<Settlement> filter = s -> true;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (IllegalArgumentException e1) {
                if ("open".equalsIgnoreCase(args[0])) {
                    tag = "open";
                    filter = s -> s.getJoinType() == JoinType.ANYONE;
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
        String finalTag = tag;
        int finalPage = page;
        Predicate<Settlement> finalFilter = filter;
        Utils.runAsync(() -> {
            List<Settlement> settlements = SettlementManager.getInstance().getSettlements().stream()
                    .filter(finalFilter)
                    .sorted(Settlement::compareTo)
                    .toList();

            int pages = (int) Math.ceil(settlements.size() / (double) Config.ENTRIES_PER_PAGE);
            getMessage("head").tell(sender,
                    "page", String.valueOf(finalPage),
                    "maxpage", String.valueOf(pages)
            );

            for (int i = (finalPage - 1) * Config.ENTRIES_PER_PAGE; i < settlements.size() && i < finalPage * Config.ENTRIES_PER_PAGE; i++) {
                Settlement v = settlements.get(i);
                Component component = getMessage("entry" + (v.isAbandoned() ? "-abandoned" : "")).getReplacedComponent(v.getReplacements(this,
                        "place", String.valueOf(i + 1),
                        "access", v.getAccess() != LandAccess.PRIVATE ? v.getAccess().getTag() : ""
                ));
                if (!v.isAbandoned()) {
                    component = component.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, getMessage("entry-hover").getReplacedComponent(
                            v.getReplacements(this)
                    )));
                    component = component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/s info " + v.getName()));
                }
                MessageUtils.tell(sender, component);
            }

            Component next = getMessage("next").getComponent();
            if (finalPage + 1 > pages) {
                next = next.color(NamedTextColor.GRAY);
            } else {
                next = next.color(NamedTextColor.AQUA);
                next = next.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/" + base + " " + alias + " " + (finalTag != null ? finalTag + " " : "") + (finalPage + 1)));
                next = next.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentUtils.parseComponent("&e>>")));
            }

            Component previous = getMessage("previous").getComponent();
            if (finalPage - 1 <= 0) {
                previous = previous.color(NamedTextColor.GRAY);
            } else {
                previous = previous.color(NamedTextColor.AQUA);
                previous = previous.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/" + base + " " + alias + " " + (finalTag != null ? finalTag + " " : "") + (finalPage - 1)));
                previous = previous.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentUtils.parseComponent("&e<<")));
            }

            Map<String, Component> replacements = Map.of(
                    "next", next,
                    "previous", previous,
                    "page", ComponentUtils.parseComponent(String.valueOf(finalPage)),
                    "maxpage", ComponentUtils.parseComponent(String.valueOf(pages))
            );
            MessageUtils.tell(sender, getMessage("footer").getReplacedComponent(replacements));
        });

        return true;
    }
}
