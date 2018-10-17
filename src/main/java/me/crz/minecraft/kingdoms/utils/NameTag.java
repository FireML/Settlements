package me.crz.minecraft.kingdoms.utils;

import org.bukkit.ChatColor;

/**
 * Objects that have a name entry in the message config
 */
public interface NameTag extends ConfigKey, HumanReadable {
    default String getTag() {
        Message message = Message.get(getKey() + ".name");
        if (message.exists()) {
            return ChatColor.stripColor(message.getText());
        }
        return humanizedName();
    }
}
