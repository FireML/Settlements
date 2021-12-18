package de.themoep.settlements.utils;

import de.themoep.settlements.resident.Resident;

import java.util.List;

public interface Targetable {

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

    boolean isMember(Resident resident);

    String getName();

    IdObject getId();
}
