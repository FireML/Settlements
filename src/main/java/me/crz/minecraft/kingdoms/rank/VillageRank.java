package me.crz.minecraft.kingdoms.rank;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.crz.minecraft.kingdoms.utils.ConfigKey;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public enum VillageRank implements Rankable, NameTag {
    CHIEF,
    ELDER,
    RECRUITER,
    BUILDER,
    RESIDENT;

    private Multimap<VillageState, String> titles = MultimapBuilder.enumKeys(VillageState.class).arrayListValues().build();

    private static Map<String, VillageRank> byTitle = new HashMap<>();

    static {
        for (VillageRank rank : values()) {
            for (VillageState state : VillageState.values()) {
                List<String> titles = Message.get(state.getKey() + ".rank." + rank.getKey()).getTextList().stream().map(ChatColor::stripColor).collect(Collectors.toList());
                rank.titles.putAll(state, titles);
                byTitle.put(rank.name().toLowerCase(), rank);
                byTitle.putAll(titles.stream().collect(Collectors.toMap(String::toLowerCase, t -> rank)));
            }
        }
    }

    public static VillageRank get(String string) {
        return byTitle.get(string.toLowerCase());
    }

    public List<String> getTitles(Village village) {
        return village != null ? (List<String>) titles.get(village.getState()) : new ArrayList<>();
    }

    public String getTag(Village village) {
        Collection<String> titles = getTitles(village);
        return titles != null && !titles.isEmpty() ? titles.iterator().next() : getTag();
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

    public int getTitleIndex(Village village, String name) {
        int i = 0;
        for (String title : getTitles(village)) {
            if (title.equalsIgnoreCase(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public String getTitle(Village village, int index) {
        List<String> titles = getTitles(village);
        if (titles.size() > index) {
            return titles.get(index);
        }
        return getTag(village);
    }
}
