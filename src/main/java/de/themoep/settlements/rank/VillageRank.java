package de.themoep.settlements.rank;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Next;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageState;
import de.themoep.settlements.perk.PerkEffect;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class VillageRank extends Rank<Village> implements Next<VillageRank>, Rankable<VillageRank> {

    private Multimap<VillageState, String> titles = MultimapBuilder.enumKeys(VillageState.class).arrayListValues().build();

    public VillageRank(String name, ConfigurationSection config) {
        super(name, config);
        for (VillageState state : VillageState.values()) {
            List<String> titles = Message.get(state.getKey() + ".rank." + getKey()).getTextList().stream().map(ChatColor::stripColor).collect(Collectors.toList());
            this.titles.putAll(state, titles);
        }
        PerkEffect.EffectInfluenced.register(new PerkEffect.EffectInfluenced(name));
    }

    @Override
    public VillageRank getInheritedRank() {
        return get(getInherits());
    }

    @Override
    public Collection<VillageRank> getInheritedRanks() {
        List<VillageRank> ranks = new ArrayList<>();
        VillageRank rank = this;
        while (rank.getInherits() != null) {
            ranks.add(rank.getInheritedRank());
            rank = rank.getInheritedRank();
        }
        return ranks;
    }

    @Override
    public VillageRank getParentRank() {
        return get(getParent());
    }

    public static VillageRank get(String string) {
        return RankManager.getInstance().getVillageRank(string);
    }

    public static VillageRank getDefault() {
        return RankManager.getInstance().getVillageDefaultRank();
    }

    public static VillageRank getChief() {
        return RankManager.getInstance().getVillageChiefRank();
    }

    public boolean isChief() {
        return this == getChief();
    }

    public static boolean isToggleablePermission(String permission) {
        return RankManager.getInstance().isToggleableVillagePerm(permission);
    }

    public static Collection<PermissionGroup> getPermGroups() {
        return RankManager.getInstance().getVillagePermGroups();
    }

    public static PermissionGroup getPermGroup(String group) {
        return RankManager.getInstance().getVillagePermGroup(group);
    }

    public static VillageRank valueOf(String string) throws IllegalArgumentException {
        VillageRank rank = get(string);
        if (rank == null) {
            throw new IllegalArgumentException("No VillageRank with name " + string);
        }
        return rank;
    }

    @Override
    public List<String> getTitles(Village village) {
        return village != null ? getTitles(village.getState()) : new ArrayList<>();
    }

    public List<String> getTitles(VillageState state) {
        return (List<String>) titles.get(state);
    }

    public static VillageRank[] values() {
        return RankManager.getInstance().getVillageRanks().toArray(new VillageRank[0]);
    }

    @Override
    public VillageRank[] getValues() {
        return values();
    }
}
