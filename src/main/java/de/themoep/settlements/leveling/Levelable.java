package de.themoep.settlements.leveling;

import de.themoep.settlements.perk.LevelPerk;
import de.themoep.settlements.perk.Vaultable;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.NameTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public interface Levelable<T extends LevelInfo> extends Vaultable, NameTag {

    int getLevel();

    void setLevel(int level);

    @Nullable
    T getLevelInfo();

    Optional<T> getNextLevel();

    default boolean canLevelUp() {
        if (isMaxLevel()) {
            return false;
        }
        return has(getNextLevelRequirements());
    }

    @NotNull
    default Map<String, Long> getNextLevelRequirements() {
        return getNextLevel().map(LevelInfo::getRequirements).orElse(Collections.emptyMap());
    }

    boolean isMaxLevel();

    boolean checkLevelUp(Resident resident);

    Collection<? extends LevelPerk> getPerks(int level);

    default double getNextLevelProgress() {
        Map<String, Long> requirements = getNextLevelRequirements();
        if (requirements.isEmpty()) {
            return 1;
        }

        double totalProgress = 0;

        for (Map.Entry<String, Long> requirement : requirements.entrySet()) {
            totalProgress += Math.min(1, (double) getAmount(requirement.getKey()) / requirement.getValue());
        }

        return totalProgress / requirements.size();
    }
}
