package me.crz.minecraft.kingdoms.leveling;

import me.crz.minecraft.kingdoms.perk.Vaultable;
import me.crz.minecraft.kingdoms.resident.Resident;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public interface Levelable<T extends LevelInfo> extends Vaultable {

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
}
