package me.crz.minecraft.kingdoms.utils;

import de.themoep.minedown.MineDown;
import de.themoep.minedown.Replacer;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Resident;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        String text = rawText;
        if (!exists) {
            text = "&cUnknown message &6" + key;
        }
        try {
            components = new MineDown(text).autoAddUrlPrefix(false).toComponent();
            if (!TextComponent.toPlainText(components).isEmpty()) {
                String newText = TextComponent.toLegacyText(components);
                if (newText.startsWith(ChatColor.WHITE.toString()) && !text.startsWith(ChatColor.WHITE.toString())) {
                    newText = newText.substring(ChatColor.WHITE.toString().length());
                }
                text = newText;
            } else {
                text = MessageUtils.replaceColorCodes(text);
            }
        } catch (IllegalArgumentException e) {
            components = TextComponent.fromLegacyText(text);
            text = MessageUtils.replaceColorCodes(text);
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Message with key " + key + " has an invalid MineDown format! " + e.getMessage());
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

    public TitleBuilder getTitle() {
        List<String> lines = getTextList();
        TitleBuilder builder = new TitleBuilder().withTitle(lines.get(0));
        if (lines.size() > 1) {
            builder.withSubtitle(lines.get(1));
            if (lines.size() > 2) {
                builder.withActionbar(lines.get(2));
            }
        }
        return builder;
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

    public void tell(Resident resident, String... replacements) {
        MessageUtils.tell(resident, this, replacements);
    }

    public void broadcast(String... replacements) {
        MessageUtils.broadcast(this, replacements);
    }

    private static Map<String, Message> messages = new HashMap<>();

    public static Message get(String key) {
        Message message = messages.get(key.toLowerCase());
        if (message == null) {
            message = new Message(key.toLowerCase());
            messages.put(key.toLowerCase(), message);
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
