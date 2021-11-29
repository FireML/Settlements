package de.themoep.settlements.integration.denizen.objects;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;

import java.util.Locale;

public class ResidentTag implements ObjectTag {

    // <--[ObjectType]
    // @name ResidentTag
    // @prefix resident
    // @base ElementTag
    // @format
    // The identity format for residents is their name
    // For example, 'resident@Phoenix616'.
    //
    // @plugin Settlements
    // @description
    // A ResidentTag represents a resident.
    //
    // -->

    public static final ObjectTagProcessor<ResidentTag> tagProcessor = new ObjectTagProcessor<>();

    private static final String PREFIX = "Resident";

    private final Resident resident;
    private String prefix = PREFIX;

    public static ResidentTag valueOf(String string) {
        return ResidentTag.valueOf(string, null);
    }

    @Fetchable("resident")
    public static ResidentTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        string = string.replaceFirst(PREFIX.toLowerCase(Locale.ROOT) + "@", "");

        Resident resident = ResidentManager.getInstance().getResident(string);
        if (resident == null) {
            return null;
        }

        return new ResidentTag(resident);
    }

    public static boolean matches(String arg) {
        arg = arg.replaceFirst(PREFIX.toLowerCase(Locale.ROOT) + "@", "");
        return ResidentManager.getInstance().getResident(arg) != null;
    }

    public ResidentTag(Resident resident) {
        this.resident = resident;
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
    public String getObjectType() {
        return PREFIX;
    }

    @Override
    public String identify() {
        return PREFIX.toLowerCase(Locale.ROOT) + "@" + resident.getName();
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

    public Resident getResident() {
        return resident;
    }

    public boolean equals(ResidentTag residentTag) {
        return residentTag.getResident().getUUID().equals(getResident().getUUID());
    }

    public static void registerTags() {
        // <--[tag]
        // @attribute <Resident.player>
        // @returns PlayerTag
        // @plugin Settlements
        // @description
        // Returns the player connected to this resident.
        // -->
        tagProcessor.registerTag(PlayerTag.class, "player",
                (attribute, object) -> new PlayerTag(object.getResident().getPlayer()));

        // <--[tag]
        // @attribute <Resident.name>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the name of the Resident.
        // -->
        tagProcessor.registerTag(ElementTag.class, "name",
                (attribute, object) -> new ElementTag(object.getResident().getName()));

        // <--[tag]
        // @attribute <Resident.title>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the title of the Resident.
        // -->
        tagProcessor.registerTag(ElementTag.class, "title",
                (attribute, object) -> new ElementTag(object.getResident().getDisplayTitle()));

        // <--[tag]
        // @attribute <Resident.settlement>
        // @returns SettlementTag
        // @plugin Settlements
        // @description
        // Returns the settlement that a Resident belongs to.
        // -->
        TagRunnable.ObjectInterface<ResidentTag, SettlementTag> settlementGetter = ((attribute, object) -> {
            if (!object.getResident().hasSettlement()) {
                return null;
            }
            return new SettlementTag(object.getResident().getSettlement());
        });
        tagProcessor.registerTag(SettlementTag.class, "settlement", settlementGetter);
        tagProcessor.registerTag(SettlementTag.class, "village", settlementGetter);
        tagProcessor.registerTag(SettlementTag.class, "kingdom", settlementGetter);
    }
}
