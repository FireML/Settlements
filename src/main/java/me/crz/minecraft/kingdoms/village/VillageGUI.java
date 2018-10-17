package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.utils.ConfigMessages;

public abstract class VillageGUI implements ConfigMessages {
    private final String name;

    public VillageGUI(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public String type() {
        return "gui";
    }
}
