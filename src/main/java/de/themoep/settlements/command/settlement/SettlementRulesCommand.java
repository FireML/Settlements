package de.themoep.settlements.command.settlement;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.FontWidthUtil;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class SettlementRulesCommand extends SettlementSubCommand {
    public SettlementRulesCommand(SubCommand parent) {
        super(parent, "rules");
        registerCommand(new SettlementSubCommand(this, "accept") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (resident.getSettlement().useGuestRank() && resident.getSettlementRank() == resident.getSettlement().getDefaultRank()) {
                    resident.setSettlementRank(resident.getSettlement().getDefaultRank().previous());
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
        List<BaseComponent[]> pages = new ArrayList<>();

        String rawRules = Message.get("rules.book-text").getReplacedText(
                "rules", FontWidthUtil.wrap(resident.getSettlement().getRules(), FontWidthUtil.getMaxBookWidth(), true)
        );

        String[] lines = rawRules.split("\n");

        int pageLines = 0;
        StringBuilder pageBuilder = new StringBuilder();
        for (String line : lines) {
            pageLines++;
            if (pageLines < 14) {
                pageBuilder.append("\n").append(line);
            } else {
                pages.add(MessageUtils.replacePlaceholders(TextComponent.fromLegacyText(pageBuilder.toString()),
                        "accept", resident.hasPermission("settlement.rules.accept") ? Message.get("rules.accept-action").getComponents() : TextComponent.fromLegacyText("")));
                pageLines = 0;
                pageBuilder = new StringBuilder();
            }
        }

        String page = pageBuilder.toString();
        if (!page.isEmpty()) {
            pages.add(MessageUtils.replacePlaceholders(TextComponent.fromLegacyText(page),
                    "accept", resident.hasPermission("settlement.rules.accept") ? Message.get("rules.accept-action").getComponents() : TextComponent.fromLegacyText("")));
        }

        if (!pages.isEmpty()) {
            resident.getPlayer().openBook(SettlementsPlugin.getInstance().getServerAdapter().createBook(
                    "Rules",
                    resident.getSettlement().getStateTag() + " " + resident.getSettlement().getName(),
                    pages
            ));
        } else {
            resident.tell("Error while getting the rules!");
        }
        return true;
    }
}
