package de.themoep.settlements.utils.config.message;

import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.messages.message.ComponentMessage;

public interface ConfigMessages {

    /**
     * @return The root key of the messages for this class. Leave empty to use the root of messages.yml.
     */
    @NonNull String rootKey();

    default @NonNull ComponentMessage<?, ?> getMessage(@NonNull String key) {
        if (rootKey().isEmpty()) {
            return ConfigMessage.get(key);
        }
        String finalKey = rootKey() + "." + key;
        return ConfigMessage.get(finalKey);
    }

    default @NonNull ComponentMessage<?, ?> getMessage(@NonNull String key, @NonNull String def) {
        if (rootKey().isEmpty()) {
            return ConfigMessage.get(key, def);
        }
        String finalKey = rootKey() + "." + key;
        return ConfigMessage.get(finalKey, def);
    }

}
