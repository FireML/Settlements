package de.themoep.settlements.command.settlement;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import uk.firedev.poleislib.utils.StringUtils;

import java.util.Comparator;
import java.util.stream.Collectors;

public class SettlementResListCommand extends SettlementSubCommand {
    public SettlementResListCommand(SubCommand parent) {
        super(parent, "reslist", "roster", "members", "memberlist");
    }

    @Override
    public boolean run(Resident sender, String base, String alias, String... args) {
        Utils.runAsync(() -> {
            sender.tell(getMessage("head").getText());
            String entry = getMessage("entry").getText();
            String online = getMessage("online").getText();
            String offline = getMessage("offline").getText();
            String separator = getMessage("separator").getText();
            Multimap<SettlementRank, Resident> rankMap = MultimapBuilder.treeKeys().arrayListValues().build();
            for (Resident resident : sender.getSettlement().getResidents()) {
                rankMap.put(resident.getSettlementRank(), resident);
            }
            for (SettlementRank rank : rankMap.keySet()) {
                sender.tell(getMessage("rank").getText(),
                        "rank", rank.getTag(sender.getSettlement()),
                        "residents", rankMap.get(rank).stream()
                                .sorted(Comparator.comparing(Resident::getSettlementJoin))
                                .map(res -> StringUtils.parsePlaceholders(entry,
                                        "online", res.getPlayer() != null ? online : offline,
                                        "name", res.getName(),
                                        "rank", res.getSettlementRank().getTag(res.getSettlement()),
                                        "title", res.getSettlementTitle(),
                                        "displaytitle", res.getDisplayTitle()
                                ))
                                .collect(Collectors.joining(separator))
                );
            }
        });
        return true;
    }
}
