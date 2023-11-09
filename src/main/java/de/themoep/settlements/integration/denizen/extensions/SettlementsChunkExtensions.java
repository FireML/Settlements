package de.themoep.settlements.integration.denizen.extensions;

import com.denizenscript.denizen.objects.ChunkTag;
import de.themoep.settlements.integration.denizen.objects.LandTag;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;

public class SettlementsChunkExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <ChunkTag.land>
        // @returns LandTag
        // @plugin Settlements
        // @description
        // Returns the LandTag in that chunk, if it exists
        // -->
        ChunkTag.tagProcessor.registerTag(LandTag.class, "land", (attribute, chunk) -> {
            OwnedLand land = LandManager.getInstance().getLand(chunk.getChunk());
            if (land != null) {
                return new LandTag(land);
            }
            return null;
        });
    }
}