package de.themoep.settlements.integration.denizen.objects;

import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.Bukkit;

public class LandTag implements ObjectTag {

    // <--[ObjectType]
    // @name LandTag
    // @prefix land
    // @base ChunkTag
    // @format
    // The identity format for land is <x>,<z>,<world>
    // For example, 'land@1,2,world'.
    //
    // @description
    // A LandTag represents an OwnedLand object in Settlements.
    //
    // -->

    //////////////////
    //    Object Fetcher
    ////////////////

    @Fetchable("land")
    public static LandTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }
        string = CoreUtilities.toLowerCase(string).replace("land@", "");
        String[] split = string.split(",");
        OwnedLand land = LandManager.getInstance().getLand(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Bukkit.getWorld(split[2]));
        if (land != null) {
            return new LandTag(land);
        }
        return null;
    }

    public static boolean matches(String arg) {
        if (CoreUtilities.toLowerCase(arg).startsWith("land@")) {
            return true;
        }
        String[] split = arg.split(",");
        OwnedLand land = LandManager.getInstance().getLand(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Bukkit.getWorld(split[2]));
        return land != null;
    }

    /////////////////////
    //   Constructors
    //////////////////

    public LandTag(ChunkTag chunkTag) {
        this.land = LandManager.getInstance().getLand(chunkTag.getChunk());
    }

    public LandTag(OwnedLand land) {
        this.land = land;
    }

    /////////////////////
    //   Instance Fields/Methods
    /////////////////

    private OwnedLand land;

    public OwnedLand getLand() {
        return land;
    }

    /////////////////////
    //  ObjectTag Methods
    ///////////////////

    private String prefix = "Land";

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
        return "land@" + land.getX() + "," + land.getZ() + "," + land.getWorldName();
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public LandTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public static void register() {

        // <--[tag]
        // @attribute <LandTag.empire>
        // @returns EmpireTag
        // @description
        // Gets the land's empire
        // -->
        tagProcessor.registerTag(EmpireTag.class, "empire", (attribute, object) -> {
            LandOwner owner = object.land.getLandOwner();
            if (owner instanceof Empire e) {
                return new EmpireTag(e);
            }
            Settlement s = (Settlement) owner;
            return new EmpireTag(s.getEmpire());
        });

        // <--[tag]
        // @attribute <LandTag.settlement>
        // @returns SettlementTag
        // @description
        // Gets the land's settlement
        // -->
        tagProcessor.registerTag(SettlementTag.class, "settlement", (attribute, object) -> {
            LandOwner owner = object.land.getLandOwner();
            if (owner instanceof Empire e) {
                return new SettlementTag(e.getCapital());
            }
            Settlement s = (Settlement) owner;
            return new SettlementTag(s);
        });
    }

    public static ObjectTagProcessor<LandTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }
}