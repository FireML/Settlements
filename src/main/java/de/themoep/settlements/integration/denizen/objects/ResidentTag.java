package de.themoep.settlements.integration.denizen.objects;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.Adjustable;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;

import java.util.ArrayList;
import java.util.List;

public class ResidentTag implements ObjectTag, Adjustable {

    public static ObjectTagProcessor<ResidentTag> tagProcessor = new ObjectTagProcessor<>();

    // <--[ObjectType]
    // @name ResidentTag
    // @prefix resident
    // @base ElementTag
    // @format
    // The identity format for settlements is their names.
    // For example, 'resident@FireML'.
    //
    // @plugin Settlements
    // @description
    // A ResidentTag represents a resident.
    //
    // -->

    @Fetchable("resident")
    public static ResidentTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        ////////
        // Match Resident name

        string = string.replace("resident@", "");
        Resident resident = ResidentManager.getInstance().getResident(string);
        if (resident == null) {
            return null;
        }
        return new ResidentTag(resident);

    }

    Resident resident = null;

    public ResidentTag(Resident resident) {
        if (resident != null) {
            this.resident = resident;
        }
        else {
            Debug.echoError("Resident referenced is null!");
        }
    }

    public static boolean matches(String arg) {
        if (valueOf(arg, CoreUtilities.noDebugContext) != null) {
            return true;
        }
        return false;
    }

    String prefix = "resident";

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
        return "resident@" + resident.getName();
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

    public Resident getResident() {
        return resident;
    }

    public boolean equals(Resident resident1) {
        return resident1.equals(this.resident);
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        mechanism.echoError("Cannot apply Properties to a resident!");
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public static void register() {
        PropertyParser.registerPropertyTagHandlers(ResidentTag.class, tagProcessor);

        // <--[tag]
        // @attribute <ResidentTag.player>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the player connected to this resident.
        // -->
        tagProcessor.registerTag(PlayerTag.class, "player", (attribute, object) -> new PlayerTag(object.getResident().getPlayer()));

        // <--[tag]
        // @attribute <ResidentTag.name>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the name of the resident.
        // -->
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> new ElementTag(object.getResident().getName()));

        // <--[tag]
        // @attribute <ResidentTag.title>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the title of the resident.
        // -->
        tagProcessor.registerTag(ElementTag.class, "title", (attribute, object) -> new ElementTag(object.getResident().getDisplayTitle()));

        // <--[tag]
        // @attribute <ResidentTag.settlement>
        // @returns SettlementTag
        // @plugin Settlements
        // @description
        // Returns the settlement of the resident.
        // -->
        tagProcessor.registerTag(SettlementTag.class, "title", (attribute, object) -> new SettlementTag(object.getResident().getSettlement()));

        // <--[tag]
        // @attribute <ResidentTag.chat_channels>
        // @returns ListTag(ElementTag)
        // @plugin Settlements
        // @description
        // Returns the chat channels of the resident.
        // -->
        tagProcessor.registerTag(ListTag.class, "chat_channels", (attribute, object) -> {
            List<ElementTag> channels = new ArrayList<>();
            for (ChatChannel channel : object.getResident().getChannels()) {
                channels.add(new ElementTag(channel.humanizedName()));
            }
            return new ListTag(channels);
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

    }
}