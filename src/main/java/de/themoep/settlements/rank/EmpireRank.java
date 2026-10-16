package de.themoep.settlements.rank;

import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Next;
import de.themoep.settlements.utils.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class EmpireRank extends Rank<Empire, EmpireRank> implements Next<EmpireRank>, Rankable {

    private List<String> titles;

    public EmpireRank(String name, ConfigurationSection config) {
        super(name, config);
        titles = Message.get("empire.rank." + getKey()).getTextList().stream().map(StringUtils::stripColors).toList();
    }

    @Override
    public EmpireRank getInheritedRank() {
        return get(getInherits());
    }

    @Override
    public Collection<EmpireRank> getInheritedRanks() {
        List<EmpireRank> ranks = new ArrayList<>();
        EmpireRank rank = this;
        while (rank.getInherits() != null) {
            ranks.add(rank.getInheritedRank());
            rank = rank.getInheritedRank();
        }
        return ranks;
    }

    @Override
    public EmpireRank getParentRank() {
        return get(getParent());
    }

    @Override
    public List<String> getTitles(Empire empire) {
        return titles;
    }

    public static EmpireRank get(String name) {
        return RankManager.getInstance().getEmpireRank(name);
    }

    public static EmpireRank getDefault() {
        return RankManager.getInstance().getEmpireDefaultRank();
    }

    public static EmpireRank getLeader() {
        return RankManager.getInstance().getEmpireLeaderRank();
    }

    public static boolean isToggleablePermission(String permission) {
        return RankManager.getInstance().isToggleableEmpirePerm(permission);
    }

    public static Collection<PermissionGroup> getPermGroups() {
        return RankManager.getInstance().getEmpirePermGroups();
    }

    public static PermissionGroup getPermGroup(String group) {
        return RankManager.getInstance().getEmpirePermGroup(group);
    }

    public static EmpireRank valueOf(String string) throws IllegalArgumentException {
        EmpireRank rank = get(string);
        if (rank == null) {
            throw new IllegalArgumentException("No EmpireRank with name " + string);
        }
        return rank;
    }

    public static EmpireRank[] values() {
        return RankManager.getInstance().getEmpireRanks().toArray(new EmpireRank[0]);
    }

    @Override
    public EmpireRank[] getValues() {
        return values();
    }
}
