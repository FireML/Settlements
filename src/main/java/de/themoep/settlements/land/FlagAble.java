package de.themoep.settlements.land;

import java.util.Map;

public interface FlagAble extends BuildStateable {

    default BuildState getUseState(Use u) {
        return getUseState(UseFlagManager.getInstance().get(u), u);
    }

    default BuildState getUseState(UseFlag useFlag) {
        return getUseState(useFlag, useFlag.getUseTypes().iterator().next());
    }

    BuildState getUseState(UseFlag useFlag, Use u);

    Map<Use, BuildState> getUseStates();

    default void setUseGroupState(UseGroup group, BuildState state) {
        for (UseFlag flag : group.getUseFlags()) {
            setUseState(flag, state);
        }
    }

    default BuildState getUseGroupState(UseGroup group) {
        BuildState groupState = null;
        for (UseFlag flag : group.getUseFlags()) {
            BuildState useState = getUseState(flag);
            if (groupState == null) {
                groupState = useState;
            } else if (groupState != useState) {
                groupState = null;
                break;
            }
        }
        return groupState;
    }

    boolean setUseState(Use u, BuildState state);

    default boolean setUseState(UseFlag u, BuildState state) {
        boolean r = false;
        state = u.getDefault(getDefaultBuildState()) == state ? null : state;
        for (Use useType : u.getUseTypes()) {
            r |= setUseState(useType, state);
        }
        return r;
    }

    boolean unsetUseState(UseFlag u);

    boolean hasUseState(Use u);

    default boolean hasUseState(UseFlag useFlag) {
        for (Use u : useFlag.getUseTypes()) {
            if (hasUseState(u)) {
                return true;
            }
        }
        return false;
    }
}
