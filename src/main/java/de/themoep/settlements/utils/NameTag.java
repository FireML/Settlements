package de.themoep.settlements.utils;

import org.bukkit.ChatColor;
import uk.firedev.poleislib.utils.StringUtils;

import java.util.Locale;

/**
 * Objects that have a name entry in the message config
 */
public interface NameTag extends ConfigKey, HumanReadable {
    static String getTag(Enum e) {
        Message message = Message.get(ConfigKey.getKey(e) + ".name");
        if (!message.exists()) {
            message = Message.get("names." + e.getClass().getSimpleName().toLowerCase(Locale.ROOT).replace("$$", ".") + "." + ConfigKey.getKey(e));
        }
        if (message.exists()) {
            return StringUtils.stripColors(message.getText());
        }
        return HumanReadable.humanizedName(e);
    }

    default String getTag() {
        Message message = Message.get(getKey() + ".name");
        if (!message.exists()) {
            message = Message.get(getKey());
        }
        if (!message.exists()) {
            String simpleClassName = getClass().getSimpleName().toLowerCase(Locale.ROOT).replace("$$", ".");
            message = Message.get("names." + simpleClassName + "." + getKey());
            if (!message.exists()) {
                message = Message.get("names." + simpleClassName + "." + name());
            }
        }
        if (message.exists()) {
            return StringUtils.stripColors(message.getText());
        }
        return humanizedName();
    }
}
