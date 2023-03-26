package de.themoep.settlements.integration.denizen.objects;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Adjustable;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.Use;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.world.SimpleChunkLocation;

import java.util.Locale;
import java.util.UUID;

public class LandTag implements ObjectTag, Adjustable {

    // <--[ObjectType]
    // @name LandTag
    // @prefix resident
    // @base ElementTag
    // @format
    // The identity format for land is their chunk coordinates
    // For example, 'land@world,x,z'.
    //
    // @plugin Settlements
    // @description
    // A LandTag represents a claimed land.
    //
    // -->

    public static final ObjectTagProcessor<LandTag> tagProcessor = new ObjectTagProcessor<>();

    private static final String PREFIX = "land";

    private final OwnedLand land;
    private String prefix = PREFIX;


    public static LandTag valueOf(String string) {
        return LandTag.valueOf(string, null);
    }

    @Fetchable("resident")
    public static LandTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        string = string.replaceFirst(PREFIX.toLowerCase(Locale.ROOT) + "@", "");

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

    public static boolean matches(String arg) {
        arg = arg.replaceFirst(PREFIX.toLowerCase(Locale.ROOT) + "@", "");
        return ResidentManager.getInstance().getResident(arg) != null;
    }

    public LandTag(OwnedLand land) {
        this.land = land;
    }

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
        return PREFIX.toLowerCase(Locale.ROOT) + "@" + land.getWorldName() + "," + land.getX() + "," + land.getZ();
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

    public String toString() {
        return identify();
    }

    public OwnedLand getLand() {
        return land;
    }

    public boolean equals(LandTag residentTag) {
        return residentTag.getLand().equals(getLand());
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <land.settlement_owned>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns whether this land is owned by a settlement.
        // -->
        tagProcessor.registerTag(ElementTag.class, "settlement_owned",
                (attribute, object) -> new ElementTag(object.getLand().getLandOwner() instanceof Settlement));

        // <--[tag]
        // @attribute <land.settlement>
        // @returns SettlementTag
        // @plugin Settlements
        // @description
        // Returns the owning settlement.
        // -->
        tagProcessor.registerTag(SettlementTag.class, "settlement", ((attribute, object) -> {
            if (object.getLand().getLandOwner() instanceof Settlement) {
                return new SettlementTag((Settlement) object.getLand().getLandOwner());
            }
            return null;
        }));

        // <--[tag]
        // @attribute <land.empire_owned>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns whether this land is owned by an empire.
        // -->
        tagProcessor.registerTag(ElementTag.class, "empire_owned",
                (attribute, object) -> new ElementTag(object.getLand().getLandOwner() instanceof Empire));

        // <--[tag]
        // @attribute <land.empire>
        // @returns EmpireTag
        // @plugin Settlements
        // @description
        // Returns the owning empire.
        // -->
        tagProcessor.registerTag(EmpireTag.class, "empire", ((attribute, object) -> {
            if (object.getLand().getLandOwner() instanceof Empire) {
                return new EmpireTag((Empire) object.getLand().getLandOwner());
            }
            return null;
        }));

        // <--[tag]
        // @attribute <land.is_plot>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns whether the land is a plot that can be owned.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_plot",
                (attribute, object) -> new ElementTag(object.getLand().isPlot()));

        // <--[tag]
        // @attribute <land.is_embassy>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns whether the land is an embassy.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_embassy",
                (attribute, object) -> new ElementTag(object.getLand().isEmbassy()));

        // <--[tag]
        // @attribute <land.player_owned>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns whether the land is owned by a player.
        // -->
        tagProcessor.registerTag(ElementTag.class, "player_owned",
                (attribute, object) -> new ElementTag(object.getLand().getPlotOwner() != null));

        // <--[tag]
        // @attribute <land.owner>
        // @returns PlayerTag
        // @plugin Settlements
        // @description
        // Returns the owner of the plot.
        // -->
        tagProcessor.registerTag(PlayerTag.class, "owner", ((attribute, object) -> {
            if (object.getLand().getPlotOwner() == null) {
                return null;
            }
            return new PlayerTag(object.getLand().getPlotOwner());
        }));

        // <--[tag]
        // @attribute <land.price>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the price of the plot.
        // -->
        tagProcessor.registerTag(ElementTag.class, "price",
                (attribute, object) -> new ElementTag(object.getLand().getPrice()));

        // <--[tag]
        // @attribute <land.name>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the name of the plot.
        // -->
        tagProcessor.registerTag(ElementTag.class, "name",
                (attribute, object) -> new ElementTag(object.getLand().getName()));

        // <--[tag]
        // @attribute <land.description>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the description of the plot.
        // -->
        tagProcessor.registerTag(ElementTag.class, "description",
                (attribute, object) -> new ElementTag(object.getLand().getDescription()));

        for (PlotMode mode : PlotMode.values()) {
            // <--[tag]
            // @attribute <Settlement.mode_name>
            // @returns ElementTag
            // @plugin Settlements
            // @description
            // Returns whether the mode is enabled for the settlement.
            // -->
            tagProcessor.registerTag(ElementTag.class, "mode_" + mode.name().toLowerCase(Locale.ROOT),
                    (attribute, object) -> new ElementTag(object.getLand().getMode(mode)));
        }

        for (Use use : Use.values()) {
            // <--[tag]
            // @attribute <Settlement.use_name>
            // @returns ElementTag
            // @plugin Settlements
            // @description
            // Returns the use flag state of the settlement.
            // -->
            tagProcessor.registerTag(ElementTag.class, "use_" + use.name().toLowerCase(Locale.ROOT),
                    (attribute, object) -> new ElementTag(object.getLand().getUseState(use).name().toLowerCase(Locale.ROOT)));
        }
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object LandTag
        // @name owner
        // @input LandTag(String)
        // @plugin Settlements
        // @description
        // Sets the owner of a land.
        // @tags
        // <LandTag.owner>
        // -->
        if (mechanism.matches("owner")) {
            String name = mechanism.getValue().asString();
            if (name.isEmpty()) {
                LandManager.getInstance().setPlotOwner(land, null);
                return;
            }
            Resident resident;
            if (name.length() == 36) {
                try {
                    resident = ResidentManager.getInstance().getResident(UUID.fromString(name));
                } catch (IllegalArgumentException e) {
                    resident = ResidentManager.getInstance().getResident(name);
                }
            } else {
                resident = ResidentManager.getInstance().getResident(name);
            }
            if (resident == null) {
                mechanism.echoError("No player found for '" + name + "'");
                return;
            }
            LandManager.getInstance().setPlotOwner(land, resident.getUUID());
        }
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        Debug.echoError("Cannot apply properties to a land!");
    }
}
