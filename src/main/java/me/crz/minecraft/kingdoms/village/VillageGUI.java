package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.utils.Message;

public abstract class VillageGUI {
    private final String name;

    public VillageGUI(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Message getMessage(String key) {
        Message message = Message.get("gui." + getName().toLowerCase() + "." + key);
        if (message.exists()) {
            return message;
        }
        if (Message.get("gui." + key).exists()) {
            return Message.get("gui." + key);
        }
        return message;
    }

    public String getText(String key) {
        return getMessage(key).getText();
    }
}
