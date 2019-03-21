package me.crz.minecraft.kingdoms.command.village;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Comparator;
import java.util.stream.Collectors;

public class VillageResListCommand extends VillageSubCommand {
    public VillageResListCommand(SubCommand parent) {
        super(parent, "reslist", "roster", "members", "memberlist");
    }

    @Override
    public boolean run(Resident sender, String base, String alias, String... args) {
        Utils.runAsync(() -> {
            sender.tell(getMessage("head"));
            String entry = getMessage("entry").getText();
            String online = getMessage("online").getText();
            String offline = getMessage("offline").getText();
            String separator = getMessage("separator").getText();
            Multimap<VillageRank, Resident> rankMap = MultimapBuilder.treeKeys().arrayListValues().build();
            for (Resident resident : sender.getVillage().getResidents()) {
                rankMap.put(resident.getVillageRank(), resident);
            }
            for (VillageRank rank : rankMap.keySet()) {
                sender.tell(getMessage("rank"),
                        "rank", rank.getTag(sender.getVillage()),
                        "residents", rankMap.get(rank).stream()
                                .sorted(Comparator.comparing(Resident::getVillageJoin))
                                .map(res -> MessageUtils.replacePlaceholders(entry,
                                        "online", res.getPlayer() != null ? online : offline,
                                        "name", res.getName(),
                                        "rank", res.getVillageRank().getTag(res.getVillage()),
                                        "title", res.getTitle()
                                ))
                                .collect(Collectors.joining(separator))
                );
            }
        });
        return true;
    }
}
