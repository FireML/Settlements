package de.themoep.settlements.utils.config.message;

import de.themoep.settlements.utils.config.ConfigManager;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.messages.config.PaperConfigReader;
import uk.firedev.daisylib.messages.message.ListMessage;
import uk.firedev.daisylib.messages.message.Message;
import uk.firedev.daisylib.messages.message.SingleMessage;

import java.util.List;

public class ConfigMessage {

    private static SingleMessage getPrefix() {
        return Message.message(getReader(), "prefix", "<gray>[Settlements]</gray> ").toSingleMessage();
    }

    public static @NonNull Message<?, ?> get(@NonNull String key) {
        return get(key, "<red>Unknown Message: <yellow>" + key);
    }

    public static @NonNull Message<?, ?> get(@NonNull String key, @NonNull String def) {
        return Message.message(getReader(), key, def)
            .replace("{prefix}", getPrefix());
    }

    public static @NonNull Message<?, ?> getError(@NonNull String key) {
        return get("error." + key);
    }

    public static @NonNull ListMessage emptyListMessage() {
        return Message.message(List.of());
    }

    public static @NonNull SingleMessage emptySingleMessage() {
        return Message.message(Component.empty());
    }

    private static PaperConfigReader getReader() {
        return new PaperConfigReader(ConfigManager.getInstance().getMessagesConfig());
    }

}
