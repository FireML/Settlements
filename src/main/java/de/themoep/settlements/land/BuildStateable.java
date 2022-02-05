package de.themoep.settlements.land;

public interface BuildStateable {

    void setBuildState(BuildState buildState);

    BuildState getBuildState();

    void save();

    String getStateTag();

    default String getBuildTag() {
        return "Build";
    }

    BuildState getDefaultBuildState();

    BuildState.AppliesTo getBuildStateType();

    default boolean acceptsState(BuildState state) {
        return state.appliesTo(getBuildStateType());
    }

    FlagAble getParent();
}
