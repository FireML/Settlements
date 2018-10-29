package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.village.Resident;

import java.util.List;

public interface BonusClaimsHolder {

    int getBonusClaims();

    void setBonusClaims(int bonusClaims);

    default void addBonusClaims(int add) {
        setBonusClaims(getBonusClaims() + add);
    }

    default boolean removeBonusClaims(int remove) {
        if (getBonusClaims() >= remove) {
            setBonusClaims(getBonusClaims() - remove);
            return true;
        }
        return false;
    }

    List<Resident> getOnlineResidents();
}
