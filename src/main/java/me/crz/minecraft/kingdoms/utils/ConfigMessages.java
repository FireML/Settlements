package me.crz.minecraft.kingdoms.utils;

public interface ConfigMessages extends ConfigKey {
    String type();

    default String getText(String key) {
        return getMessage(key).getText();
    }

    default Message getMessage(String key) {
        String path = getConfigPath();
        Message message = Message.get(path + "." + key);
        if (message.exists()) {
            return message;
        }
        while (path.contains(".")) {
            path = path.substring(0, path.indexOf('.'));
            Message m = Message.get(path + "." + key);
            if (m.exists()) {
                return m;
            }
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
