package de.themoep.settlements.land;

public class UseFlagSetter implements BuildStateable {
    private final FlagAble flagAble;
    private final UseFlag useFlag;

    public UseFlagSetter(FlagAble flagAble, UseFlag useFlag) {
        this.flagAble = flagAble;
        this.useFlag = useFlag;
    }

    @Override
    public void setBuildState(BuildState buildState) {
        flagAble.setUseState(useFlag, buildState);
    }

    @Override
    public BuildState getBuildState() {
        return flagAble.getUseState(useFlag);
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
        return "use " + useFlag.getTag();
    }

    @Override
    public BuildState getDefaultBuildState() {
        return useFlag.getDefault(flagAble.getDefaultBuildState());
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
