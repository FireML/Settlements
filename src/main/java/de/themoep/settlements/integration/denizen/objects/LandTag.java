package de.themoep.settlements.integration.denizen.objects;

import com.denizenscript.denizencore.objects.Adjustable;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.world.SimpleChunkLocation;

public class LandTag implements ObjectTag, Adjustable {

    public static ObjectTagProcessor<LandTag> tagProcessor = new ObjectTagProcessor<>();

    // <--[ObjectType]
    // @name LandTag
    // @prefix settlement
    // @base ElementTag
    // @format
    // The identity format for settlements is their names.
    // For example, 'settlement@my_settlement'.
    //
    // @plugin Settlements
    // @description
    // A LandTag represents a settlement.
    //
    // -->

    @Fetchable("settlement")
    public static LandTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        string = string.replace("land@", "");

        String[] parts = string.split(",", 3);
        if (parts.length < 3) {
            return null;
        }

        try {
            int x = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            OwnedLand land = LandManager.getInstance().getLand(new SimpleChunkLocation(parts[0], x, z));
            if (land == null) {
                return null;
            }

            return new LandTag(land);
        } catch (IllegalArgumentException e) {
            return null;
        }

    }

    OwnedLand land = null;

    public LandTag(OwnedLand land) {
        if (land != null) {
            this.land = land;
        }
        else {
            Debug.echoError("Land referenced is null!");
        }
    }

    public static boolean matches(String arg) {
        if (valueOf(arg, CoreUtilities.noDebugContext) != null) {
            return true;
        }
        return false;
    }

    String prefix = "land";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String identify() {
        return "land@" + land.getWorldName() + "," + land.getX() + "," + land.getZ();
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public ObjectTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String toString() {
        return identify();
    }

    public OwnedLand getLand() {
        return land;
    }

    public boolean equals(OwnedLand land1) {
        return land1.equals(this.land);
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        mechanism.echoError("Cannot apply Properties to an empire!");
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object LandTag
        // @name owner
        // @input ElementTag(String)
        // @plugin Settlements
        // @description
        // Sets the owner of the land.
        // @tags
        // <LandTag.owner>
        // -->
        if (mechanism.matches("owner")) {
            String name = mechanism.getValue().asString();
            Resident resident = ResidentManager.getInstance().getResident(name);
            if (resident == null) {
                mechanism.echoError("No player found for '" + name + "'");
                return;
            }
            LandManager.getInstance().setPlotOwner(land, resident.getUUID());
        }

        tagProcessor.processMechanism(this, mechanism);
        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }
}