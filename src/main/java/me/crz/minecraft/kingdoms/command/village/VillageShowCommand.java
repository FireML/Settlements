package me.crz.minecraft.kingdoms.command.village;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;

import me.crz.minecraft.kingdoms.rank.VillageRank;

import java.text.DecimalFormat;

import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.land.LandManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageShowCommand extends SubCommand {
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");

    public VillageShowCommand(SubCommand parent) {
        super(parent, "show [<name>]");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (args.length == 0) {
            Village village = VillageManager.getInstance().getVillageFromPlayer(player);
            if (village == null) {
                MessageUtils.tell(player, getError("village-required"));
                return false;
            }
            Utils.runAsync(() -> MessageUtils.tell(player, getMessage("general"), getReplacements(village)));
        } else if (args.length == 1) {
            String name = args[0];
            Utils.runAsync(() -> {
                Village village2 = VillageManager.getInstance().getVillageFromName(name);
                Resident target = ResidentManager.getInstance().getResident(name);
                if (target != null) {
                    Village playerVillage = VillageManager.getInstance().getVillageFromResident(target);
                    MessageUtils.tell(player, getMessage("general"), getReplacements(playerVillage));
                } else if (village2 != null) {
                    MessageUtils.tell(player, getMessage("general"), getReplacements(village2));
                } else {
                    MessageUtils.tell(player, getError("village-doesnt-exist"), "name", args[0]);
                }
            });
        } else {
            return false;
        }
        return true;
    }

    private String[] getReplacements(Village village) {
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
                "access", village.getAccess().getTag(),
                "chiefRank", VillageRank.CHIEF.getTag(village),
                "chief", ResidentManager.getInstance().getResident(village.getChief()).getName(),
                "residents", getResidentsToString(village.getResidents(), village)
        };
    }

    public String yesOrNo(boolean b) {
        return b ? "Yes" : "No";
    }

    private String getResidentsToString(List<Resident> residents, Village village) {
        Message rankMessage = getMessage("residents.rank");
        String entry = getMessage("residents.entry").getText();
        String online = getMessage("residents.online").getText();
        String offline = getMessage("residents.offline").getText();
        String separator = getMessage("residents.separator").getText();
        Multimap<VillageRank, Resident> rankMap = MultimapBuilder.enumKeys(VillageRank.class).arrayListValues().build();
        for (Resident resident : residents) {
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
