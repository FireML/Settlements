package me.crz.minecraft.kingdoms.rank;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Next;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VillageRank extends Rank implements Next<VillageRank> {
    @Deprecated
    public static VillageRank CHIEF = new VillageRank("", null);
    @Deprecated
    public static VillageRank ELDER = new VillageRank("", null);
    @Deprecated
    public static VillageRank RECRUITER = new VillageRank("", null);
    @Deprecated
    public static VillageRank BUILDER = new VillageRank("", null);
    @Deprecated
    public static VillageRank RESIDENT = new VillageRank("", null);

    private Multimap<VillageState, String> titles = MultimapBuilder.enumKeys(VillageState.class).arrayListValues().build();

    public VillageRank(String name, ConfigurationSection config) {
        super(name, config);
        for (VillageState state : VillageState.values()) {
            List<String> titles = Message.get(state.getKey() + ".rank." + getKey()).getTextList().stream().map(ChatColor::stripColor).collect(Collectors.toList());
            this.titles.putAll(state, titles);
        }
    }

    @Override
    public VillageRank getInheritedRank() {
        return get(getInherits());
    }

    @Override
    public VillageRank getParentRank() {
        return get(getParent());
    }

    public static VillageRank get(String string) {
        return VillageManager.getInstance().getRank(string);
    }

    public static VillageRank valueOf(String string) throws IllegalArgumentException {
        VillageRank rank = get(string);
        if (rank == null) {
            throw new IllegalArgumentException("No VillageRank with name " + string);
        }
        return rank;
    }

    public List<String> getTitles(Village village) {
        return village != null ? getTitles(village.getState()) : new ArrayList<>();
    }

    public List<String> getTitles(VillageState state) {
        return (List<String>) titles.get(state);
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

    public static VillageRank[] values() {
        return VillageManager.getInstance().getRanks().toArray(new VillageRank[0]);
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

    @Override
    public VillageRank[] getValues() {
        return values();
    }
}
