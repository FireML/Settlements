package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.utils.Next;
import org.bukkit.configuration.ConfigurationSection;

public class EmpireRank extends Rank implements Next<EmpireRank> {
    @Deprecated
    public static EmpireRank LEADER = new EmpireRank("", null);
    @Deprecated
    public static EmpireRank SENATOR = new EmpireRank("", null);
    @Deprecated
    public static EmpireRank MEMBER = new EmpireRank("", null);

    public EmpireRank(String name, ConfigurationSection config) {
        super(name, config);
    }

    @Override
    public EmpireRank getInheritedRank() {
        return get(getInherits());
    }

    public static EmpireRank get(String name) {
        return EmpireManager.getInstance().getRank(name);
    }

    public static EmpireRank valueOf(String string) throws IllegalArgumentException {
        EmpireRank rank = get(string);
        if (rank == null) {
            throw new IllegalArgumentException("No EmpireRank with name " + string);
        }
        return rank;
    }

    public static EmpireRank[] values() {
        return EmpireManager.getInstance().getRanks().toArray(new EmpireRank[0]);
    }

    @Override
    public EmpireRank[] getValues() {
        return values();
    }
}
