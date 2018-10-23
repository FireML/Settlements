package me.crz.minecraft.kingdoms.rank;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.crz.minecraft.kingdoms.utils.ConfigKey;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Rankable;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageState;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public enum VillageRank implements Rankable, ConfigKey {
    CHIEF,
    ELDER,
    RECRUITER,
    BUILDER,
    RESIDENT;

    private Multimap<VillageState, String> titles = MultimapBuilder.enumKeys(VillageState.class).linkedHashSetValues().build();

    private static Map<String, VillageRank> byTitle = new HashMap<>();

    static {
        for (VillageRank rank : values()) {
            for (VillageState state : VillageState.values()) {
                List<String> titles = Message.get(state.getKey() + ".rank." + rank.getKey()).getTextList();
                rank.titles.putAll(state, titles);
                byTitle.put(rank.name().toLowerCase(), rank);
                byTitle.putAll(titles.stream().collect(Collectors.toMap(String::toLowerCase, t -> rank)));
            }
        }
    }

    public static VillageRank get(String string) {
        return byTitle.get(string.toLowerCase());
    }

    public Collection<String> getTitles(Village village) {
        return village != null ? titles.get(village.getState()) : new LinkedHashSet<>();
    }

    public String getTag(Village village) {
        Collection<String> titles = getTitles(village);
        return titles != null && !titles.isEmpty() ? titles.iterator().next() : "";
    }

    public boolean isTitleAvailable(Village village, String title) {
        return title != null && getAvailableTitles(village).contains(title);
    }

    private Collection<String> getAvailableTitles(Village village) {
        Set<String> titles = new LinkedHashSet<>();
        if (village != null) {
            for (int i = ordinal(); i < values().length; i++) {
                titles.addAll(values()[i].getTitles(village));
            }
        }
        return titles;
    }
}
