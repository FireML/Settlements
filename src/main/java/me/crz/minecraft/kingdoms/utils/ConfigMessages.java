package me.crz.minecraft.kingdoms.utils;

public interface ConfigMessages extends ConfigKey {
    String type();

    default String getText(String key) {
        return getMessage(key).getText();
    }

    default Message getMessage(String key) {
        return getMessage(null, key);
    }
    default Message getMessage(String extraPath, String key) {
        String path = getConfigPath() + (extraPath != null && !extraPath.isEmpty() ? "." + extraPath : "");
        Message message = Message.get(path + (key.isEmpty() ? "" : "." + key));
        if (message.exists()) {
            return message;
        }
        String[] pathParts = path.split("\\.");
        if (pathParts.length > 1) {
            for (int sub = 1; sub + 1 < pathParts.length; sub++) {
                for (int i = pathParts.length - sub; i > 0; i--) {
                    StringBuilder p = new StringBuilder();
                    for (int j = 0; j + sub < pathParts.length; j++) {
                        if (j == i) {
                            j+=sub;
                        }
                        p.append(".").append(pathParts[j]);
                    }
                    Message m = Message.get(p + (key.isEmpty() ? "" : "." + key));
                    if (m.exists()) {
                        return m;
                    }
                }
            }
        }
        Message keyMessage = Message.get(key);
        if (keyMessage.exists()) {
            return keyMessage;
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
