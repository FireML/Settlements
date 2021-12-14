package de.themoep.settlements.command.settlement;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.FontWidthUtil;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import de.themoep.settlements.command.SubCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class SettlementRulesCommand extends SettlementSubCommand {
    public SettlementRulesCommand(SubCommand parent) {
        super(parent, "rules");
        registerCommand(new SubCommand(this, "accept") {
            @Override
            public boolean run(CommandSender sender, String base, String alias, String... args) {
                // TODO: Settlement validation
                return false;
            }
        });
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        List<BaseComponent[]> pages = new ArrayList<>();

        String rawRules = Message.get("rules.book").getRawText().replace(
                "{rules}", FontWidthUtil.wrap(resident.getSettlement().getRules(), FontWidthUtil.getMaxBookWidth(), true)
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
                        "accept", Message.get("rules.accept-action").getComponents()));
                pageLines = 0;
                pageBuilder = new StringBuilder();
            }
        }

        resident.getPlayer().openBook(SettlementsPlugin.getInstance().getServerAdapter().createBook(
                "Rules",
                resident.getSettlement().getStateTag() + " " + resident.getSettlement().getName(),
                pages
        ));
        return true;
    }
}
