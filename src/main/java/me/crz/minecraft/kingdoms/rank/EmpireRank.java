package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.utils.NameTag;

public enum EmpireRank implements NameTag, Rankable {
    LEADER,
    SENATOR,
    MEMBER;

    public static EmpireRank get(String name) {
        try {
            return EmpireRank.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
