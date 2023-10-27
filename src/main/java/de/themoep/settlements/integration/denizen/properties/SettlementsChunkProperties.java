package de.themoep.settlements.integration.denizen.properties;

import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import de.themoep.settlements.integration.denizen.objects.LandTag;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import org.bukkit.Chunk;

public class SettlementsChunkProperties implements Property {

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "Location";
    }

    public static boolean describes(ObjectTag object) {
        return object instanceof ChunkTag;
    }

    public static SettlementsChunkProperties getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new SettlementsChunkProperties((ChunkTag) object);
        }
    }

    public static final String[] handledTags = new String[] {
            "land"
    };

    public static final String[] handledMechs = new String[] {
    };

    public SettlementsChunkProperties(ChunkTag chunk) {
        this.chunk = chunk.getChunk();
    }

    Chunk chunk;

    public static void register() {

        // <--[tag]
        // @attribute <ChunkTag.land>
        // @returns LandTag
        // @plugin Settlements
        // @description
        // Returns the LandTag in that chunk, if it exists
        // -->
        PropertyParser.registerTag(SettlementsChunkProperties.class, LandTag.class, "land", (attribute, object) -> {
            OwnedLand land = LandManager.getInstance().getLand(object.chunk);
            if (land != null) {
                return new LandTag(land);
            }
            return null;
        });
    }
}