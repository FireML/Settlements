package de.themoep.settlements.command.settlement;

import com.google.common.collect.ImmutableMap;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.LogType;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.List;

public class SettlementHistoryCommand extends SettlementSubCommand {
    private final LogType type;

    public SettlementHistoryCommand(SubCommand parent, LogType type) {
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
            int pages = DatabaseManager.getInstance().getLogPageCount(resident.getSettlement(), resident, type);
            getMessage("head").tell(resident,
                    "page", String.valueOf(page),
                    "maxpage", String.valueOf(pages)
            );
            List<String[]> history = DatabaseManager.getInstance().getLog(resident.getSettlement(), resident, page, type);
            for (String[] entry : history) {
                getMessage("entry").tell(resident, entry);
            }

            ComponentBuilder next = new ComponentBuilder("")
                    .append(getMessage("next").getReplacedComponents("next", String.valueOf(page + 1)));
            if (page + 1 > pages) {
                next.color(ChatColor.GRAY);
            } else {
                next.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + base + " " + alias + " " + (page + 1)));
                next.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(ChatColor.YELLOW + ">>"))));
            }
            ComponentBuilder previous = new ComponentBuilder("")
                    .append(getMessage("previous").getReplacedComponents("previous", String.valueOf(page - 1)));
            if (page - 1 <= 0) {
                previous.color(ChatColor.GRAY);
            } else {
                previous.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + base + " " + alias + " " + (page - 1)));
                previous.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(ChatColor.YELLOW + "<<"))));
            }

            MessageUtils.tell(resident.getPlayer(), MessageUtils.replacePlaceholders(getMessage("footer").getReplacedComponents(
                    "page", String.valueOf(page),
                    "maxpage", String.valueOf(pages)
            ), ImmutableMap.of(
                    "page", TextComponent.fromLegacyText(String.valueOf(page)),
                    "maxpage", TextComponent.fromLegacyText(String.valueOf(pages)),
                    "next", next.create(),
                    "previous", previous.create()
            )));
        });
        return true;
    }

}
