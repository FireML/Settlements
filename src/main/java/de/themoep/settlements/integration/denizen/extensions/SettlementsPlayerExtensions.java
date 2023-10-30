package de.themoep.settlements.integration.denizen.extensions;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import de.themoep.settlements.integration.denizen.objects.ResidentTag;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;

public class SettlementsPlayerExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <PlayerTag.resident>
        // @returns ResidentTag
        // @plugin Settlements
        // @description
        // Returns the Player's ResidentTag
        // -->
        PlayerTag.tagProcessor.registerTag(ResidentTag.class, "resident", (attribute, player) -> new ResidentTag(player.getPlayerEntity()));
    }
}