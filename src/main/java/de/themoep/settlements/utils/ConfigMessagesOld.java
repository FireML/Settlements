package de.themoep.settlements.utils;

import de.themoep.settlements.perk.SettlementPerk;
import de.themoep.settlements.settlement.Settlement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public interface ConfigMessagesOld extends ConfigKey {
    String type();

    default String getText(String key, String... replacements) {
        return StringUtils.parseColors(getMessage(key).getReplacedText(replacements));
    }

    default String getUnlockText(Settlement settlement, String unlockable) {
        unlockable = unlockable.toLowerCase(Locale.ROOT);
        if (unlockable.startsWith("settlements.")) {
            unlockable = unlockable.substring("settlements.".length());
        }
        if (unlockable.contains(settlement.getKey() + ".")) {
            unlockable = unlockable.replace(settlement.getKey() + ".", "");
        }
        for (SettlementPerk perk : settlement.getAvailablePerks()) {
            if (perk.getUnlockables().contains(unlockable)) {
                if (perk.getLevel() > 1) {
                    return getText("unlockables.locked.upgrade",
                            "type", perk.getTag(),
                            "level", String.valueOf(perk.getLevel())
                    );
                }
                return getText("unlockables.locked.unlock", "type", perk.getTag());
            }
        }
        return getText("unlockables.locked.level", "state", settlement.getStateTag());
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
            String startPart = pathParts[0];

            Message m = getMessage(startPart, Arrays.asList(pathParts).subList(1, pathParts.length), key);
            if (m != null && m.exists()) {
                return m;
            }
        }
        if (!key.isEmpty()) {
            Message keyMessage = Message.get(key);
            if (keyMessage.exists()) {
                return keyMessage;
            }
        }
        return message;
    }

    default Message getMessage(String start, List<String> partList, String key) {
        Message message = Message.get(start + (partList.isEmpty() ? "" : "." + String.join(".", partList)) + (key.isEmpty() ? "" : "." + key));
        if (message.exists()) {
            return message;
        }
        for (int i = 0; i < partList.size(); i++) {
            List<String> clone = new ArrayList<>();
            for (int j = 0; j < partList.size(); j++) {
                if (j != i) {
                    clone.add(partList.get(j));
                }
            }
            Message m = getMessage(start, clone, key);
            if (m != null && m.exists()) {
                return m;
            }
        }
        return null;
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

    default String getErrorText(String key, String... replacements) {
        return getError(key).getReplacedText(replacements);
    }
}
