package de.themoep.settlements.integration.denizen.properties;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import de.themoep.settlements.integration.denizen.objects.LandTag;
import de.themoep.settlements.integration.denizen.objects.ResidentTag;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import org.bukkit.Location;

public class SettlementsLocationProperties implements Property {

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "Location";
    }

    public static boolean describes(ObjectTag object) {
        return object instanceof LocationTag;
    }

    public static SettlementsLocationProperties getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new SettlementsLocationProperties((LocationTag) object);
        }
    }

    public static final String[] handledTags = new String[] {
            "land"
    };

    public static final String[] handledMechs = new String[] {
    };

    public SettlementsLocationProperties(LocationTag location) {
        this.location = location;
    }

    Location location;

    public static void register() {

        // <--[tag]
        // @attribute <LocationTag.land>
        // @returns LandTag
        // @plugin Settlements
        // @description
        // Returns the LandTag in that location, if it exists
        // -->
        PropertyParser.registerTag(SettlementsLocationProperties.class, LandTag.class, "land", (attribute, object) -> {
            OwnedLand land = LandManager.getInstance().getLand(object.location.getChunk());
            if (land != null) {
                return new LandTag(land);
            }
            return null;
        });
    }
}