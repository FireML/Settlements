package me.crz.minecraft.kingdoms.utils;

public enum WeatherState implements NameTag, Next<WeatherState> {
    CLEAR, RAIN, THUNDER, NORMAL;

    @Override
    public WeatherState[] getValues() {
        return WeatherState.values();
    }
}
