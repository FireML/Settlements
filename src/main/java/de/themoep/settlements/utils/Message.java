package de.themoep.settlements.utils;

import de.themoep.minedown.adventure.MineDown;
import de.themoep.minedown.adventure.MineDownParser;
import de.themoep.minedown.adventure.Replacer;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.config.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class Message {
    private final String rawText;
    private final String text;
    private final String key;
    private final boolean exists;

    protected Message(String key) {
        this.key = key;
        if (ConfigManager.getInstance().getMessagesConfig().isString(key)) {
            rawText = ConfigManager.getInstance().getMessagesConfig().getString(key);
            exists = true;
        } else if (ConfigManager.getInstance().getMessagesConfig().isList(key)) {
            List<String> list = ConfigManager.getInstance().getMessagesConfig().getStringList(key);
            rawText = list.stream().map(String::valueOf).collect(Collectors.joining("\n"));
            exists = true;
        } else {
            rawText = "";
            exists = false;
        }
        String text = rawText != null ? rawText : "";
        if (!exists) {
            text = "&cUnknown Message &e" + key;
        }
        try {
            String checkText = ComponentUtils.toString(new MineDown(text).disable(MineDownParser.Option.LEGACY_COLORS).autoAddUrlPrefix(false).toComponent());
            if (!checkText.isEmpty()) {
                text = checkText;
            }
        } catch (IllegalArgumentException e) {
            SettlementsPlugin.getInstance().getLogging().warn("Message with key " + key + " has an invalid MineDown format! " + e.getMessage());
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
        return StringUtils.parseColors(rawText);
    }

    public String getText() {
        return StringUtils.parseColors(text);
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
        return ComponentUtils.parseComponent(getText());
    }

    public Component getReplacedComponent(String... replacements) {
        return ComponentUtils.parsePlaceholders(getComponent(), replacements);
    }

    public Component getReplacedComponent(Map<String, Component> replacements) {
        return ComponentUtils.parsePlaceholders(getComponent(), replacements);
    }

    public String getReplacedText(String... replacements) {
        return StringUtils.parsePlaceholders(text, replacements);
    }

    public String getReplacedText(Map<String, String> replacements) {
        return StringUtils.parsePlaceholders(text, replacements);
    }

    public List<String> getReplacedTextList(String... replacements) {
        List<String> returnList = new ArrayList<>();
        for (String s : getTextList()) {
            returnList.add(StringUtils.parsePlaceholders(s, replacements));
        }
        return returnList;
    }

    public List<String> getReplacedTextList(Map<String, String> replacements) {
        List<String> returnList = new ArrayList<>();
        for (String s : getTextList()) {
            returnList.add(StringUtils.parsePlaceholders(s, replacements));
        }
        return returnList;
    }

    public MessageReplacer getReplacer() {
        return new MessageReplacer(this);
    }

    public void tell(CommandSender sender, String... replacements) {
        MessageUtils.tell(sender, this.getReplacedComponent(replacements));
    }

    public void tell(Collection<Resident> residents, String... replacements) {
        MessageUtils.tellResidents(residents, this.getReplacedComponent(replacements));
    }

    public void tell(Targetable targetable, String... replacements) {
        MessageUtils.tellResidents(targetable.getOnlineResidents(), this.getReplacedComponent(replacements));
    }

    public void actionbar(Targetable targetable, String... replacements) {
        MessageUtils.sendActionBar(targetable.getOnlineResidents(), this.getReplacedComponent(replacements));
    }

    public void tell(Resident resident) {
        MessageUtils.tell(resident, this.getComponent());
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

    public static class MessageReplacer extends Replacer {
        private Message message;

        public MessageReplacer(Message message) {
            this.message = message;
            placeholderPrefix("{");
            placeholderSuffix("}");
        }

        public MessageReplacer replace(String placeholder, Component value) {
            super.replace(placeholder, value);
            return this;
        }

        public MessageReplacer replace(String... replacements) {
            super.replace(replacements);
            return this;
        }

        public MessageReplacer replace(Map<String, ?> replacements) {
            super.replace(replacements);
            return this;
        }

        public Component getComponents() {
            return replaceIn(message.getComponent());
        }

        public String getText() {
            return replaceIn(message.getText());
        }

        public MessageReplacer copy() {
            return new MessageReplacer(message).copy(this);
        }

        public MessageReplacer copy(MessageReplacer replacer) {
            super.copy(replacer);
            message = replacer.message;
            return this;
        }
    }
}
