package me.crz.minecraft.kingdoms;

import me.crz.minecraft.kingdoms.utils.ConfigMessages;

public abstract class KingdomsGUI implements ConfigMessages {
    private final String name;

    public KingdomsGUI(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public String type() {
        return "gui";
    }
}
