package de.themoep.settlements.npc;

import com.google.common.collect.Table;
import de.themoep.settlements.utils.ConfigKey;
import de.themoep.settlements.utils.NameTag;
import de.themoep.settlements.utils.Utils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public enum NpcType implements NameTag, ConfigKey {
    MANAGER,
    QUARTER_MASTER,
    BANKER,
    ELDER,
    TRAVELING_MERCHANT,
    BLACKSMITH,
    AUCTIONEER,
    BLACK_MARKET,
    ;

    private final Set<AppliesTo> appliesTo;

    NpcType(AppliesTo... appliesTo) {
        if (appliesTo.length > 0) {
            this.appliesTo = EnumSet.noneOf(AppliesTo.class);
            Collections.addAll(this.appliesTo, appliesTo);
        } else {
            this.appliesTo = EnumSet.allOf(AppliesTo.class);
        }
    }

    public boolean appliesTo(AppliesTo to) {
        return appliesTo.contains(to);
    }

    public static NpcType parse(String string) {
        for (Table.Cell<String, NpcType, NpcConfig> config : NpcManager.getInstance().getConfigs()) {
            if (config.getValue().getName().equalsIgnoreCase(string)
                    || config.getValue().getName().equalsIgnoreCase(
                            string.replace('-', ' ').replace('_', ' '))) {
                return config.getColumnKey();
            }
        }
        return Utils.parseEnum(NpcType.class, string);
    }

    public enum AppliesTo implements ConfigKey {
        SETTLEMENT,
        EMPIRE,
        ;

        /**
         * @throws IllegalArgumentException
         */
        public static AppliesTo fromKey(String key) {
            return valueOf(key.toUpperCase(Locale.ROOT).replace('-', '_'));
        }
    }
}
