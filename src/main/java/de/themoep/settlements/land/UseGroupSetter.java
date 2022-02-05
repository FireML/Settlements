package de.themoep.settlements.land;

public class UseGroupSetter implements BuildStateable {
    private final FlagAble flagAble;
    private final UseGroup group;

    public UseGroupSetter(FlagAble flagAble, UseGroup group) {
        this.flagAble = flagAble;
        this.group = group;
    }

    @Override
    public void setBuildState(BuildState buildState) {
        flagAble.setUseGroupState(group, buildState);
    }

    @Override
    public BuildState getBuildState() {
        return flagAble.getUseGroupState(group);
    }

    @Override
    public void save() {
        flagAble.save();
    }

    @Override
    public String getStateTag() {
        return flagAble.getStateTag();
    }

    @Override
    public String getBuildTag() {
        return "use " + group.getTag();
    }

    @Override
    public BuildState getDefaultBuildState() {
        return group.getDefault(flagAble.getDefaultBuildState());
    }

    @Override
    public BuildState.AppliesTo getBuildStateType() {
        return flagAble.getBuildStateType();
    }

    @Override
    public FlagAble getParent() {
        return flagAble;
    }
}
