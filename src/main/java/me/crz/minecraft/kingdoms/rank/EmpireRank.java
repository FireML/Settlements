package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.utils.Next;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EmpireRank extends Rank implements Next<EmpireRank> {

    public EmpireRank(String name, ConfigurationSection config) {
        super(name, config);
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
