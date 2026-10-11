package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.UtilTime;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import de.themoep.settlements.utils.ComponentUtils;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class SettlementActiveCommand extends SettlementSubCommand {

    public SettlementActiveCommand(SubCommand parent) {
        super(parent, "active [<page>]", "activity");
    }

    @Override
    public boolean run(Resident sender, String base, String alias, String... args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                getError("not-a-number").tell(sender, "input", args[0]);
                return false;
            }
            if (page < 1) {
                page = 1;
            }
        }
        int finalPage = page;
        Utils.runAsync(() -> {
            Collection<Resident> residents = sender.getSettlement().getResidents();
            int pages = (int) Math.ceil(residents.size() / (double) Config.ENTRIES_PER_PAGE);
            getMessage("head").tell(sender,
                    "page", String.valueOf(finalPage),
                    "maxpage", String.valueOf(pages)
            );
            residents.stream()
                    .sorted((r1, r2) -> {
                        Player p1 = r1.getPlayer();
                        Player p2 = r2.getPlayer();
                        if (p1 != null && p2 == null) {
                            return -1;
                        } else if (p1 == null && p2 != null) {
                            return 1;
                        }
                        return Long.compare(r1.getLastLogin(), r2.getLastLogin());
                    })
                    .skip((long) (finalPage - 1) * Config.ENTRIES_PER_PAGE)
                    .limit(Config.ENTRIES_PER_PAGE)
                    .forEachOrdered(r -> getMessage("entry").tell(sender,
                            "title", r.getSettlementTitle(),
                            "displaytitle", r.getDisplayTitle(),
                            "rank", r.getSettlementRank().getTag(r.getSettlement()),
                            "name", r.getName(),
                            "activity", r.getPlayer() != null ? getText("online") : getText("offline", "ago", UtilTime.format(System.currentTimeMillis() - r.getLastLogin())),
                            "timestamp", Utils.TIME_FORMAT.format(new Date(r.getLastLogin()))
                    ));

            Component next = getMessage("next").getReplacedComponent("next", String.valueOf(finalPage + 1));
            if (finalPage + 1 > pages) {
                next = next.color(NamedTextColor.GRAY);
            } else {
                next = next.color(NamedTextColor.AQUA);
                next = next.clickEvent(ClickEvent.runCommand("/" + base + " " + alias + " " + (finalPage + 1)));
                next = next.hoverEvent(HoverEvent.showText(ComponentUtils.parseComponent("&e>>")));
            }
            Component previous = getMessage("previous").getReplacedComponent("previous", String.valueOf(finalPage - 1));
            if (finalPage - 1 <= 0) {
                previous = previous.color(NamedTextColor.GRAY);
            } else {
                previous = previous.color(NamedTextColor.AQUA);
                previous = previous.clickEvent(ClickEvent.runCommand("/" + base + " " + alias + " " + (finalPage - 1)));
                previous = previous.hoverEvent(HoverEvent.showText(ComponentUtils.parseComponent("&e<<")));
            }

            Map<String, Component> replacements = Map.of(
                    "page", ComponentUtils.parseComponent(String.valueOf(finalPage)),
                    "maxpage", ComponentUtils.parseComponent(String.valueOf(pages)),
                    "next", next,
                    "previous", previous.append(Component.text("").color(null))
            );

            MessageUtils.tell(sender.getPlayer(), ComponentUtils.parsePlaceholders(getMessage("footer").getComponent(),
                    replacements
            ));
        });
        return true;
    }
}
