package me.crz.minecraft.kingdoms.utils;

/**
 * Objects that have a name entry in the message config
 */
public interface NameTag extends ConfigKey, HumanReadable {
    default String getTag() {
        Message message = Message.get(getKey() + ".name");
        if (message.exists()) {
            return message.getText();
        }
        return humanizedName();
    }
}
