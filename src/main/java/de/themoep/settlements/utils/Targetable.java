package de.themoep.settlements.utils;

import de.themoep.settlements.resident.Resident;

import java.util.List;

public interface Targetable {

    long getBonusClaims();

    void setBonusClaims(long bonusClaims);

    default void addBonusClaims(long add) {
        setBonusClaims(getBonusClaims() + add);
    }

    default boolean removeBonusClaims(long remove) {
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
