package me.crz.minecraft.kingdoms.utils;

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
}
