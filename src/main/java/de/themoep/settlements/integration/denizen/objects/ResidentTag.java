package de.themoep.settlements.integration.denizen.objects;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.Adjustable;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.world.SimpleChunkLocation;

import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;

public class ResidentTag implements ObjectTag {

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
    public ObjectTag getObjectAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ResidentTag.player>
        // @returns PlayerTag
        // @plugin Settlements
        // @description
        // Returns the player connected to this resident.
        // -->
        if (attribute.startsWith("player")) {
            return new PlayerTag(resident.getPlayer());
        }

        // <--[tag]
        // @attribute <ResidentTag.name>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the name of the resident.
        // -->
        if (attribute.startsWith("name")) {
            return new ElementTag(resident.getName());
        }

        // <--[tag]
        // @attribute <ResidentTag.title>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the title of the resident.
        // -->
        if (attribute.startsWith("title")) {
            return new ElementTag(resident.getDisplayTitle());
        }

        // <--[tag]
        // @attribute <ResidentTag.settlement>
        // @returns SettlementTag
        // @plugin Settlements
        // @description
        // Returns the settlement of the resident.
        // -->
        if (attribute.startsWith("settlement")) {
            if (!resident.hasSettlement()) {
                return null;
            }
            return new SettlementTag(resident.getSettlement());
        }

        // <--[tag]
        // @attribute <ResidentTag.chat_channels>
        // @returns ListTag(ElementTag)
        // @plugin Settlements
        // @description
        // Returns the chat channels of the resident.
        // -->
        if (attribute.startsWith("chat_channels")) {
            List<ElementTag> channels = new ArrayList<>();
            for (ChatChannel channel : resident.getChannels()) {
                channels.add(new ElementTag(channel.humanizedName()));
            }
            return new ListTag(channels);
        }

        return new ElementTag(identify()).getObjectAttribute(attribute);
    }
}