package de.themoep.settlements.integration.denizen.extensions;

import com.denizenscript.denizen.objects.LocationTag;
import de.themoep.settlements.integration.denizen.objects.LandTag;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;

public class SettlementsLocationExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <LocationTag.land>
        // @returns LandTag
        // @plugin Settlements
        // @description
        // Returns the LandTag in that location, if it exists
        // -->
        LocationTag.tagProcessor.registerTag(LandTag.class, "land", (attribute, location) -> {
            OwnedLand land = LandManager.getInstance().getLand(location.getChunk());
            if (land != null) {
                return new LandTag(land);
            }
            return null;
        });
    }
}