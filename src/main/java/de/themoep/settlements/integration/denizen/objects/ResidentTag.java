package de.themoep.settlements.integration.denizen.objects;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.commands.world.CreateWorldCommand;
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
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import org.bukkit.entity.Player;

public class ResidentTag implements ObjectTag, Adjustable {

    // <--[ObjectType]
    // @name ResidentTag
    // @prefix resident
    // @base ChunkTag
    // @format
    // The identity format for residents is their name
    // For example, 'resident@FireML'.
    //
    // @description
    // A ResidentTag represents a Resident object in Settlements.
    //
    // -->

    //////////////////
    //    Object Fetcher
    ////////////////

    @Fetchable("resident")
    public static ResidentTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }
        string = CoreUtilities.toLowerCase(string).replace("resident@", "");
        Resident res = ResidentManager.getInstance().getResident(string);
        if (res != null) {
            return new ResidentTag(res);
        }
        return null;
    }

    public static boolean matches(String arg) {
        if (CoreUtilities.toLowerCase(arg).startsWith("resident@")) {
            return true;
        }
        Resident res = ResidentManager.getInstance().getResident(arg);
        return res != null;
    }

    /////////////////////
    //   Constructors
    //////////////////

    public ResidentTag(Resident resident) {
        this.resident = resident;
    }

    public ResidentTag(Player player) {
        this.resident = ResidentManager.getInstance().getResident(player);
    }

    /////////////////////
    //   Instance Fields/Methods
    /////////////////

    private Resident resident;

    public Resident getResident() {
        return resident;
    }

    /////////////////////
    //  ObjectTag Methods
    ///////////////////

    private String prefix = "Resident";

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
    public String toString() {
        return identify();
    }

    @Override
    public ResidentTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public static void register() {

        // <--[tag]
        // @attribute <ResidentTag.player>
        // @returns PlayerTag
        // @description
        // Gets the resident's PlayerTag
        // -->
        tagProcessor.registerTag(PlayerTag.class, "player", (attribute, object) -> new PlayerTag(object.resident.getPlayer()));

        // <--[tag]
        // @attribute <ResidentTag.name>
        // @returns ElementTag
        // @description
        // Gets the resident's name
        // -->
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> new ElementTag(object.resident.getName()));

        // <--[tag]
        // @attribute <ResidentTag.title>
        // @returns ElementTag
        // @description
        // Gets the resident's title
        // -->
        tagProcessor.registerTag(ElementTag.class, "title", (attribute, object) -> new ElementTag(object.resident.getDisplayTitle()));

        // <--[tag]
        // @attribute <ResidentTag.settlement>
        // @returns SettlementTag
        // @description
        // Gets the resident's settlement
        // -->
        tagProcessor.registerTag(SettlementTag.class, "settlement", (attribute, object) -> new SettlementTag(object.resident.getSettlement()));

        // <--[tag]
        // @attribute <ResidentTag.empire>
        // @returns EmpireTag
        // @description
        // Gets the resident's empire
        // -->
        tagProcessor.registerTag(EmpireTag.class, "empire", (attribute, object) -> new EmpireTag(object.resident.getSettlement().getEmpire()));

        // <--[tag]
        // @attribute <ResidentTag.chat_channels>
        // @returns ListTag(ElementTag)
        // @description
        // Gets the resident's chat channels
        // -->
        tagProcessor.registerTag(ListTag.class, "chat_channels", (attribute, object) -> new ListTag(object.resident.getChannels(), chatChannel -> new ElementTag(chatChannel.humanizedName())));

        // <--[tag]
        // @attribute <ResidentTag.land>
        // @returns LandTag
        // @description
        // Gets the land the player is currently standing in
        // -->
        tagProcessor.registerTag(LandTag.class, "land", (attribute, object) -> {
            OwnedLand land = LandManager.getInstance().getLand(object.resident.getPlayer().getChunk());
            if (land != null) {
                return new LandTag(land);
            }
            return null;
        });
    }

    public static ObjectTagProcessor<ResidentTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ResidentTag
        // @name add_channel
        // @input ElementTag
        // @description
        // Adds the player to a chat channel
        // @tags
        // <ResidentTag.chat_channels>
        // @example
        // - adjust <resident[FireML]> add_channel:General
        // -->
        if (mechanism.matches("add_channel")) {
            ChatChannel channel = ChatChannel.get(mechanism.getValue().asString());
            if (channel.humanizedName().equals("general") && !mechanism.getValue().asString().equals("general")) {
                channel = null;
            }
            if (channel != null) {
                resident.addChannel(channel);
                resident.save();
            }
        }

        // <--[mechanism]
        // @object ResidentTag
        // @name remove_channel
        // @input ElementTag
        // @description
        // Removes the player from a chat channel
        // @tags
        // <ResidentTag.chat_channels>
        // @example
        // - adjust <resident[FireML]> remove_channel:General
        // -->
        if (mechanism.matches("remove_channel")) {
            ChatChannel channel = ChatChannel.get(mechanism.getValue().asString());
            if (channel.humanizedName().equals("general") && !mechanism.getValue().asString().equals("general")) {
                channel = null;
            }
            if (channel != null) {
                resident.removeChannel(channel);
                resident.save();
            }
        }
    }

    public void applyProperty(Mechanism mechanism) {
        mechanism.echoError("Cannot apply properties to a resident!");
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }
}