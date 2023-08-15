package de.themoep.settlements.land;

import java.util.Map;
import java.util.Objects;

public interface ModeAble {

    /**
     * Get the mode or the default one
     * @param mode The mode to get
     * @return The value of the mode or the default mode value if not set
     */
    default boolean getMode(PlotMode mode) {
        return getModes().getOrDefault(mode, mode.getDefault());
    }

    /**
     * Set a mode
     * @param mode The mode to set
     * @param value The value to set the mode to
     * @return Whether the mode changed
     */
    default boolean setMode(PlotMode mode, boolean value) {
        if (!Objects.equals(getModes().get(mode), value)) {
            getModes().put(mode, value);
            return true;
        }
        return false;
    }

    Map<PlotMode, Boolean> getModes();

}
