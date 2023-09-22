package de.themoep.settlements.utils;

import uk.firedev.poleislib.libs.themoep.minedown.MineDown;
import uk.firedev.poleislib.libs.themoep.minedown.Replacer;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.config.ConfigManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Message {
    private final String rawText;
    private final String text;
    private final String key;
    private final boolean exists;
    private BaseComponent[] components;

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
            components = new MineDown(text).autoAddUrlPrefix(false).toComponent();
            if (!TextComponent.toPlainText(components).isEmpty()) {
                String newText = TextComponent.toLegacyText(components);
                if (newText.startsWith(ChatColor.WHITE.toString()) && !text.startsWith("&f") && !text.startsWith(ChatColor.WHITE.toString())) {
                    newText = newText.substring(ChatColor.WHITE.toString().length());
                }
                text = newText;
            } else {
                text = MessageUtils.replaceColorCodes(text);
            }
        } catch (IllegalArgumentException e) {
            components = TextComponent.fromLegacyText(text);
            text = MessageUtils.replaceColorCodes(text);
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

    public BaseComponent[] getComponents() {
        return components;
    }

    @Deprecated
    public TitleBuilder getTitle() {
        return new TitleBuilder()
                .withTitle(Message.get(getKey() + ".title"))
                .withSubtitle(Message.get(getKey() + ".subtitle"))
                .withActionbar(Message.get(getKey() + ".actionbar"));
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

    public BaseComponent[] getReplacedComponents(String placeholder, BaseComponent[] value) {
        return MessageUtils.replacePlaceholders(components, placeholder, value);
    }

    public BaseComponent[] getReplacedComponents(String... replacements) {
        return MessageUtils.replacePlaceholders(components, replacements);
    }

    public BaseComponent[] getReplacedComponents(Map<String, BaseComponent[]> replacements) {
        return MessageUtils.replacePlaceholders(components, replacements);
    }

    public MessageReplacer getReplacer() {
        return new MessageReplacer(this);
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
        MessageUtils.tell(resident, this, replacements);
    }

    public void broadcast(String... replacements) {
        MessageUtils.broadcast(this, replacements);
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

    public class MessageReplacer extends Replacer {
        private Message message;

        public MessageReplacer(Message message) {
            this.message = message;
            placeholderPrefix("{");
            placeholderSuffix("}");
        }

        public MessageReplacer replace(String placeholder, BaseComponent[] value) {
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

        public BaseComponent[] getComponents() {
            return replaceIn(message.getComponents());
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
