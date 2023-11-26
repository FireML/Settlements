package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import uk.firedev.poleislib.builders.BookBuilder;
import uk.firedev.poleislib.utils.ComponentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettlementRulesCommand extends SettlementSubCommand {
    public SettlementRulesCommand(SubCommand parent) {
        super(parent, "rules");
        registerCommand(new SettlementSubCommand(this, "accept") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (resident.getSettlement().useGuestRank() && resident.getSettlementRank() == resident.getSettlement().getDefaultRank()) {
                    resident.setSettlementRank(resident.getSettlement().getDefaultRank().previous());
                    resident.save();
                    getMessage("success").tell(resident,
                            "state", resident.getSettlement().getStateTag(),
                            "name", resident.getSettlement().getName(),
                            "rank", resident.getSettlementRank().getTag(resident.getSettlement())
                    );
                }
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        List<Component> pages = new ArrayList<>();

        String rules = resident.getSettlement().getRules();
        if (rules.length() > Config.MAX_RULES_LENGTH) {
            rules = rules.substring(0, Config.MAX_RULES_LENGTH);
        }
        String rawRules;
        if (rules.isEmpty()) {
            rawRules = Message.get("rules.default-book").getText();
        } else {
            rawRules = Message.get("rules.book-with-rules").getReplacedText(
                    "rules", resident.getSettlement().getRules()
            );
        }

        String[] lines = rawRules.split("\n");

        int pageLines = 1;
        StringBuilder pageBuilder = new StringBuilder();
        for (String line : lines) {
            pageLines++;
            if (pageLines < 14) {
                pageBuilder.append("\n").append(line);
            } else {
                Component acceptComponent = ComponentUtils.parseComponent(resident.hasPermission("settlement.rules.accept") ? Message.get("rules.accept-action").getText() : "");
                acceptComponent = acceptComponent.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentUtils.parseComponent("/s rules accept")));
                acceptComponent = acceptComponent.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/s rules accept"));
                pages.add(ComponentUtils.parseComponent(pageBuilder.toString(),
                        Map.of("accept", acceptComponent)));
                pageLines = 0;
                pageBuilder = new StringBuilder();
            }
        }

        String page = pageBuilder.toString();
        if (!page.isEmpty()) {
            Component acceptComponent = ComponentUtils.parseComponent(resident.hasPermission("settlement.rules.accept") ? Message.get("rules.accept-action").getText() : "");
            acceptComponent = acceptComponent.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentUtils.parseComponent("/s rules accept")));
            acceptComponent = acceptComponent.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/s rules accept"));
            pages.add(ComponentUtils.parseComponent(page, Map.of("accept", acceptComponent)));
        }

        if (!pages.isEmpty()) {
            new BookBuilder()
                    .withTitle(ComponentUtils.parseComponent("Rules"))
                    .withAuthor(ComponentUtils.parseComponent(resident.getSettlement().getStateTag() + " " + resident.getSettlement().getName()))
                    .withPages(pages)
                    .show(resident.getPlayer());
        } else {
            resident.tell("Error while getting the rules!");
        }
        return true;
    }
}
