package de.themoep.settlements.utils.config.message;

import de.themoep.settlements.utils.config.ConfigManager;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.messages.config.PaperConfigReader;
import uk.firedev.daisylib.messages.message.ComponentMessage;
import uk.firedev.daisylib.messages.message.ComponentSingleMessage;

public class ConfigMessage {

    private static ComponentSingleMessage getPrefix() {
        return ComponentMessage.componentMessage(getReader(), "prefix", "<gray>[Settlements]</gray> ").toSingleMessage();
    }

    public static @NonNull ComponentMessage<?, ?> get(@NonNull String key) {
        return get(key, "<red>Unknown Message: <yellow>" + key);
    }

    public static @NonNull ComponentMessage<?, ?> get(@NonNull String key, @NonNull String def) {
        return ComponentMessage.componentMessage(getReader(), key, def)
            .replace("{prefix}", getPrefix());
    }


    private static PaperConfigReader getReader() {
        return new PaperConfigReader(ConfigManager.getInstance().getMessagesConfig());
    }

}
