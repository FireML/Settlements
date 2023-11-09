package de.themoep.settlements.integration.denizen.extensions;

import com.denizenscript.denizen.objects.PlayerTag;
import de.themoep.settlements.integration.denizen.objects.ResidentTag;

public class SettlementsPlayerExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <PlayerTag.resident>
        // @returns ResidentTag
        // @plugin Settlements
        // @description
        // Returns the Player's ResidentTag
        // -->
        PlayerTag.tagProcessor.registerTag(ResidentTag.class, "resident", (attribute, player) -> new ResidentTag(player.getOfflinePlayer()));
    }
}