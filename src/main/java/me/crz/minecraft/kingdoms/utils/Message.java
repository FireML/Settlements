package me.crz.minecraft.kingdoms.utils;

import de.themoep.minedown.MineDown;
import de.themoep.minedown.Replacer;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Message {
    private final String text;
    private final String key;
    private final boolean exists;
    private BaseComponent[] components;

    Message(String key) {
        this.key = key;
        String text = "&cUnknown message &6" + key;
        if (ConfigManager.getInstance().getMessagesConfig().isString(key)) {
            exists = true;
            text = ConfigManager.getInstance().getMessagesConfig().getString(key);
        } else if (ConfigManager.getInstance().getMessagesConfig().isList(key)) {
            List<?> list = ConfigManager.getInstance().getMessagesConfig().getList(key);
            text = list.stream().map(String::valueOf).collect(Collectors.joining("\n"));
            exists = true;
        } else {
            exists = false;
        }
        this.text = MessageUtils.replaceColorCodes(text);
    }

    public boolean exists() {
        return exists;
    }

    private String getKey() {
        return key;
    }

    public String getText() {
        return text;
    }

    public BaseComponent[] getComponents() {
        if (components == null) {
            try {
                components = MineDown.parse(text);
            } catch (IllegalArgumentException e) {
                components = TextComponent.fromLegacyText(text);
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Message with key " + key + " has an invalid MineDown format! " + e.getMessage());
            }
        }
        return components;
    }

    public List<String> getTextList() {
        return Arrays.asList(text.split("\n"));
    }

    public String getReplaced(String... replaced) {
        return MessageUtils.replacePlaceholders(text, replaced);
    }

    private static Map<String, Message> messages = new HashMap<>();

    public static Message get(String key) {
        Message message = messages.get(key.toLowerCase());
        if (message == null) {
            message = new Message(key);
            messages.put(key.toLowerCase(), message);
        }
        return message;
    }
}
