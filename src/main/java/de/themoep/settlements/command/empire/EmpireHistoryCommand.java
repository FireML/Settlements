package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.LogType;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ComponentUtils;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Map;

public class EmpireHistoryCommand extends EmpireSubCommand {
    private final LogType type;

    public EmpireHistoryCommand(SubCommand parent, LogType type) {
        super(parent, "history [<page>]", "log");
        this.type = type;
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        int page;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                getError("not-a-number").tell(resident, "input", args[0]);
                return false;
            }
        } else {
            page = 1;
        }
        Utils.runAsync(() -> {
            int pages = DatabaseManager.getInstance().getLogPageCount(resident.getSettlement().getEmpire(), resident, type);
            getMessage("head").tell(resident,
                    "page", String.valueOf(page),
                    "maxpage", String.valueOf(pages)
            );
            List<String[]> history = DatabaseManager.getInstance().getLog(resident.getSettlement().getEmpire(), resident, page, type);
            for (String[] entry : history) {
                getMessage("entry").tell(resident, entry);
            }

            Component next = getMessage("next").getReplacedComponent("next", String.valueOf(page + 1));
            if (page + 1 > pages) {
                next = next.color(NamedTextColor.GRAY);
            } else {
                next = next.color(NamedTextColor.AQUA);
                next = next.clickEvent(ClickEvent.runCommand("/" + base + " " + alias + " " + (page + 1)));
                next = next.hoverEvent(HoverEvent.showText(ComponentUtils.parseComponent("&e>>")));
            }
            Component previous = getMessage("previous").getReplacedComponent("previous", String.valueOf(page - 1));
            if (page - 1 <= 0) {
                previous = previous.color(NamedTextColor.GRAY);
            } else {
                previous = previous.color(NamedTextColor.AQUA);
                previous = previous.clickEvent(ClickEvent.runCommand("/" + base + " " + alias + " " + (page - 1)));
                previous = previous.hoverEvent(HoverEvent.showText(ComponentUtils.parseComponent("&e<<")));
            }

            Map<String, Component> replacements = Map.of(
                    "page", ComponentUtils.parseComponent(String.valueOf(page)),
                    "maxpage", ComponentUtils.parseComponent(String.valueOf(pages)),
                    "next", next,
                    "previous", previous.append(Component.text("").color(null))
            );

            MessageUtils.tell(resident.getPlayer(), ComponentUtils.parsePlaceholders(getMessage("footer").getComponent(), replacements));
        });
        return true;
    }
}
