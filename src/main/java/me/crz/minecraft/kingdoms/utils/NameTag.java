package me.crz.minecraft.kingdoms.utils;

import org.bukkit.ChatColor;

/**
 * Objects that have a name entry in the message config
 */
public interface NameTag extends ConfigKey, HumanReadable {
    static String getTag(Enum e) {
        Message message = Message.get(ConfigKey.getKey(e) + ".name");
        if (!message.exists()) {
            message = Message.get("names." + e.getClass().getSimpleName().toLowerCase().replace("$$", ".") + "." + ConfigKey.getKey(e));
        }
        if (message.exists()) {
            return ChatColor.stripColor(message.getText());
        }
        return HumanReadable.humanizedName(e);
    }

    default String getTag() {
        Message message = Message.get(getKey() + ".name");
        if (!message.exists()) {
            message = Message.get("names." + getClass().getSimpleName().toLowerCase().replace("$$", ".") + "." + getKey());
        }
        if (message.exists()) {
            return ChatColor.stripColor(message.getText());
        }
        return humanizedName();
    }
}
