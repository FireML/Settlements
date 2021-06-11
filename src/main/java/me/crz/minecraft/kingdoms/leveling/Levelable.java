package me.crz.minecraft.kingdoms.leveling;

import me.crz.minecraft.kingdoms.perk.Vaultable;
import me.crz.minecraft.kingdoms.resident.Resident;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface Levelable<T extends LevelInfo> extends Vaultable {

    int getLevel();

    void setLevel(int level);

    @Nullable
    T getLevelInfo();

    default boolean canLevelUp() {
        if (isMaxLevel()) {
            return false;
        }
        return has(getNextLevelRequirements());
    }

    @NotNull
    Map<String, Long> getNextLevelRequirements();

    boolean isMaxLevel();

    boolean checkLevelUp(Resident resident);
}
