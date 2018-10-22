package me.crz.minecraft.kingdoms.command.village;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static me.crz.minecraft.kingdoms.utils.MessageUtils.yesOrNo;

public abstract class AbstractVillageInfoCommand extends SubCommand {
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");

    public AbstractVillageInfoCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
    }

    protected String[] getReplacements(Village village) {
        return new String[] {
                "name", village.getName(),
                "state", village.getState().getTag(),
                "level", String.valueOf(village.getLevel()),
                "land", String.valueOf(LandManager.getInstance().getClaimedLandSize(village)),
                "maxLand", String.valueOf(village.getMaxClaimableChunks()),
                "outposts", String.valueOf(village.getOutpostHomes().size()),
                "maxOutposts", String.valueOf(village.getMaxOutposts()),
                "pvp", yesOrNo(village.isPvp()),
                "balance", DECIMAL_FORMAT.format(village.getBank()),
                "mobSpawning", yesOrNo(village.isMobSpawning()),
                "fireSpread", yesOrNo(village.isFireSpread()),
                "explosions", yesOrNo(village.isExplosions()),
                "buff", yesOrNo(village.isBuff()),
                "bank", String.valueOf(village.getBank()),
                "bonusClaims", village.getBonusClaims() > 0 ? getMessage("bonus-claims").getReplacedText("amount", String.valueOf(village.getBonusClaims())) : "",
                "access", village.getAccess().getTag(),
                "chiefRank", VillageRank.CHIEF.getTag(village),
                "chief", ResidentManager.getInstance().getResident(village.getChief()).getName(),
                "residents", getResidentsToString(village)
        };
    }

    private String getResidentsToString(Village village) {
        Message rankMessage = getMessage("residents.rank");
        String entry = getMessage("residents.entry").getText();
        String online = getMessage("residents.online").getText();
        String offline = getMessage("residents.offline").getText();
        String separator = getMessage("residents.separator").getText();
        Multimap<VillageRank, Resident> rankMap = MultimapBuilder.enumKeys(VillageRank.class).arrayListValues().build();
        for (Resident resident : village.getResidents()) {
            rankMap.put(resident.getVillageRank(), resident);
        }
        List<String> lines = new ArrayList<>();
        for (VillageRank rank : rankMap.keySet()) {
            lines.add(rankMessage.getReplacedText(
                    "rank", rank.getTag(village),
                    "residents", rankMap.get(rank).stream()
                            .sorted(Comparator.comparing(Resident::getVillageJoin))
                            .map(res -> MessageUtils.replacePlaceholders(entry,
                                    "online", res.getPlayer() != null ? online : offline,
                                    "name", res.getName(),
                                    "rank", rank.getTag(village)
                            ))
                            .collect(Collectors.joining(separator))
            ));
        }
        return String.join("\n", lines);
    }
}
