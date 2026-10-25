package de.themoep.settlements.utils.config.message;

import de.themoep.settlements.utils.config.ConfigManager;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.messages.config.PaperConfigReader;
import uk.firedev.daisylib.messages.message.ComponentListMessage;
import uk.firedev.daisylib.messages.message.ComponentMessage;
import uk.firedev.daisylib.messages.message.ComponentSingleMessage;

import java.util.List;

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

    public static @NonNull ComponentMessage<?, ?> getError(@NonNull String key) {
        return get("error." + key);
    }

    public static @NonNull ComponentListMessage emptyListMessage() {
        return ComponentMessage.componentMessage(List.of());
    }

    public static @NonNull ComponentSingleMessage emptySingleMessage() {
        return ComponentMessage.componentMessage(Component.empty());
    }

    private static PaperConfigReader getReader() {
        return new PaperConfigReader(ConfigManager.getInstance().getMessagesConfig());
    }

}
