package de.themoep.settlements.utils;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import uk.firedev.poleislib.utils.ComponentUtils;
import uk.firedev.poleislib.utils.StringUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Message {
    private final String rawText;
    private final String text;
    private final String key;
    private final boolean exists;
    private Component component;

    protected Message(String key) {
        this.key = key;
        if (ConfigManager.getInstance().getMessagesConfig().isString(key)) {
            rawText = ConfigManager.getInstance().getMessagesConfig().getString(key);
            exists = true;
        } else if (ConfigManager.getInstance().getMessagesConfig().isList(key)) {
            List<?> list = ConfigManager.getInstance().getMessagesConfig().getList(key);
            rawText = list.stream().map(String::valueOf).collect(Collectors.joining("\n"));
            exists = true;
        } else {
            rawText = "";
            exists = false;
        }
        String text = rawText != null ? rawText : "";
        if (!exists) {
            text = "&cUnknown message &6" + key;
        }
        try {
            component = ComponentUtils.parseComponent(text);
            if (!ComponentUtils.toString(ComponentUtils.stripColors(component)).isEmpty()) {
                String newText = ComponentUtils.toString(component);
                if (newText.startsWith("§f") && !text.startsWith("&f") && !text.startsWith("§f")) {
                    newText = newText.substring("§f".length());
                }
                text = newText;
            } else {
                text = StringUtils.parseColors(text);
            }
        } catch (IllegalArgumentException e) {
            component = ComponentUtils.parseComponent(text);
            text = StringUtils.parseColors(text);
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Message with key " + key + " has an invalid MineDown format! " + e.getMessage());
        }
        this.text = text;
    }

    public boolean exists() {
        return exists;
    }

    private String getKey() {
        return key;
    }

    public String getRawText() {
        return rawText;
    }

    public String getText() {
        return text;
    }

    public String orElse(String fallback) {
        if (exists()) {
            return text;
        }
        return fallback;
    }

    public List<String> getTextList() {
        return Arrays.asList(text.split("\n"));
    }

    public Component getComponent() {
        return component;
    }

    public Component getReplacedComponent(String... replacements) {
        return ComponentUtils.parseComponent(StringUtils.parsePlaceholders(this.getText(), replacements));
    }

    public Component getReplacedComponent(Map<String, Component> replacements) {
        return ComponentUtils.parseComponent(this.getText(), replacements);
    }

    public String getReplacedText(String... replacements) {
        return MessageUtils.replacePlaceholders(text, replacements);
    }

    public String getReplacedText(Map<String, String> replacements) {
        return MessageUtils.replacePlaceholders(text, replacements);
    }

    public List<String> getReplacedTextList(String... replacements) {
        return Arrays.asList(getReplacedText(replacements).split("\n"));
    }

    public List<String> getReplacedTextList(Map<String, String> replacements) {
        return Arrays.asList(getReplacedText(replacements).split("\n"));
    }

    public void tell(CommandSender sender, String... replacements) {
        MessageUtils.tell(sender, this, replacements);
    }

    public void tell(Collection<Resident> residents, String... replacements) {
        MessageUtils.tellResidents(residents, this, replacements);
    }

    public void tell(Targetable targetable, String... replacements) {
        MessageUtils.tellResidents(targetable.getOnlineResidents(), this, replacements);
    }

    public void actionbar(Targetable targetable, String... replacements) {
        MessageUtils.sendActionBar(targetable.getOnlineResidents(), this, replacements);
    }

    public void tell(Resident resident, String... replacements) {
        MessageUtils.tell(resident, this.getComponent(), replacements);
    }

    public void broadcast(String... replacements) {
        ComponentUtils.broadcastMessage(ComponentUtils.parsePlaceholders(this.getComponent(), replacements));
    }

    private static Map<String, Message> messages = new HashMap<>();

    public static Message get(String key) {
        Message message = messages.get(key);
        if (message == null) {
            message = new Message(key.toLowerCase(Locale.ROOT));
            if (!message.exists()) {
                message = new Message(key);
            }
            messages.put(key, message);
        }
        return message;
    }

    public static Message orElse(String key, Message fallback) {
        Message message = messages.get(key.toLowerCase());
        if (message == null) {
            message = new Message(key.toLowerCase());
            messages.put(key.toLowerCase(), message);
        }
        if (!message.exists() && (fallback == null || fallback.exists())) {
            return fallback;
        }
        return message;
    }

    public static void clearCache() {
        messages.clear();
    }
}
