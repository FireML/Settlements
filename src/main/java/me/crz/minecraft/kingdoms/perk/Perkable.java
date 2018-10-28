package me.crz.minecraft.kingdoms.perk;

import me.crz.minecraft.kingdoms.Base;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public interface Perkable<T extends Perk> extends Vaultable {

    int getLevel();

    Map<String, PerkData> getPerksMap();

    T getPerk(String name);

    void runCommands(T perk);

    default boolean hasUnlocked(T perk) {
        return getPerksMap().containsKey(perk.name().toLowerCase());
    }

    default Collection<T> getPerks() {
        return getPerksMap().keySet().stream()
                .map(this::getPerk)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    default PerkData getPerkData(T perk) {
        return getPerksMap().get(perk.getKey().toLowerCase());
    }

    default boolean canUnlock(T perk) {
        return !hasUnlocked(perk) && has(perk.getUnlockCost());
    }

    default boolean canRepeat(T perk) {
        return hasUnlocked(perk) && has(perk.getRepeatCost()) && checkPerkCount(perk);
    }

    default boolean matchesLevelRequirement(T perk) {
        return getLevel() >= perk.getLevel();
    }

    default boolean checkPerkCount(T perk) {
        if (perk.getRepeat() > 0) {
            PerkData p = getPerkData(perk);
            if (p != null) {
                return p.getTimes() < perk.getRepeat();
            }
        }
        return true;
    }

    default boolean checkPerkCooldown(T perk) {
        if (perk.getCooldown() > 0) {
            PerkData p = getPerkData(perk);
            if (p != null) {
                return p.getLastUsed() + perk.getCooldown() * 1000 < System.currentTimeMillis();
            }
        }
        return true;
    }

    default boolean unlockPerk(T perk) {
        if (!hasUnlocked(perk)) {
            getPerksMap().put(perk.name().toLowerCase(), new PerkData());
            if (perk.hasEffects()) {
                Base.getLandPerks().updateEffects(false);
            }
            runCommands(perk);
            return true;
        }
        return false;
    }

    default boolean repeatPerk(T perk) {
        PerkData p = getPerkData(perk);
        if (p != null) {
            p.addUse();
            if (perk.hasEffects()) {
                Base.getLandPerks().updateEffects(false);
            }
            runCommands(perk);
            return true;
        }
        return false;
    }
}
