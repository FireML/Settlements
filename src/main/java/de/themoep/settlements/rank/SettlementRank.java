package de.themoep.settlements.rank;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.themoep.settlements.perk.PerkEffect;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementState;
import de.themoep.settlements.utils.Message;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SettlementRank extends Rank<Settlement, SettlementRank> {

    private final Multimap<SettlementState, String> titles = MultimapBuilder.enumKeys(SettlementState.class).arrayListValues().build();

    public SettlementRank(String name, ConfigurationSection config) {
        super(name, config);
        for (SettlementState state : SettlementState.values()) {
            List<String> titles = Message.get(state.getKey() + ".rank." + getKey()).getTextList().stream().map(ChatColor::stripColor).collect(Collectors.toList());
            this.titles.putAll(state, titles);
        }
        PerkEffect.EffectInfluenced.register(new PerkEffect.EffectInfluenced(name));
    }

    @Override
    public SettlementRank getInheritedRank() {
        return get(getInherits());
    }

    @Override
    public Collection<SettlementRank> getInheritedRanks() {
        List<SettlementRank> ranks = new ArrayList<>();
        SettlementRank rank = this;
        while (rank.getInherits() != null) {
            ranks.add(rank.getInheritedRank());
            rank = rank.getInheritedRank();
        }
        return ranks;
    }

    @Override
    public SettlementRank getParentRank() {
        return get(getParent());
    }

    public static SettlementRank get(String string) {
        return RankManager.getInstance().getSettlementRank(string);
    }

    public static SettlementRank getDefault() {
        return RankManager.getInstance().getSettlementDefaultRank();
    }

    public static SettlementRank getChief() {
        return RankManager.getInstance().getSettlementChiefRank();
    }

    public boolean isChief() {
        return this == getChief();
    }

    public static boolean isToggleablePermission(String permission) {
        return RankManager.getInstance().isToggleableSettlementPerm(permission);
    }

    public static Collection<PermissionGroup> getPermGroups() {
        return RankManager.getInstance().getSettlementPermGroups();
    }

    public static PermissionGroup getPermGroup(String group) {
        return RankManager.getInstance().getSettlementPermGroup(group);
    }

    public static SettlementRank valueOf(String string) throws IllegalArgumentException {
        SettlementRank rank = get(string);
        if (rank == null) {
            throw new IllegalArgumentException("No SettlementRank with name " + string);
        }
        return rank;
    }

    @Override
    public List<String> getTitles(Settlement settlement) {
        return settlement != null ? getTitles(settlement.getState()) : new ArrayList<>();
    }

    public List<String> getTitles(SettlementState state) {
        return (List<String>) titles.get(state);
    }

    public static SettlementRank[] values() {
        return RankManager.getInstance().getSettlementRanks().toArray(new SettlementRank[0]);
    }

    @Override
    public SettlementRank[] getValues() {
        return values();
    }
}
