package de.themoep.settlements.utils.config;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.firedev.daisylib.messages.config.PaperConfigReader;
import uk.firedev.daisylib.messages.message.ComponentMessage;

public class ConfigMessage {

    private static final PaperConfigReader reader = new PaperConfigReader(ConfigManager.getInstance().getMessagesConfig());

    public static @NonNull ComponentMessage<?, ?> get(@NonNull String key) {
        return ComponentMessage.componentMessage(reader, key, "<red>Unknown Message: <yellow>" + key);
    }

    public static @NonNull ComponentMessage<?, ?> get(@NonNull String key, @NonNull String def) {
        return ComponentMessage.componentMessage(reader, key, def);
    }

}
