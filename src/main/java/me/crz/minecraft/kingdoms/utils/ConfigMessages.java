package me.crz.minecraft.kingdoms.utils;

public interface ConfigMessages extends ConfigKey {
    String type();

    default String getText(String key) {
        return getMessage(key).getText();
    }

    default Message getMessage(String key) {
        String base = getBase();
        Message message = Message.get(getConfigPath() + "." + key);
        if (message.exists()) {
            return message;
        }
        if (Message.get(base + key).exists()) {
            return Message.get(base + key);
        }
        if (!base.isEmpty() && Message.get(key).exists()) {
            return Message.get(key);
        }
        return message;
    }

    default String getBase() {
        return type().isEmpty() ? "" : (type() + ".");
    }

    default String getConfigPath() {
        return getBase() + getKey().toLowerCase();
    }

    default Message getError(String key) {
        Message message = getMessage(key);
        return message.exists() ? message : getMessage("error." + key);
    }
}
