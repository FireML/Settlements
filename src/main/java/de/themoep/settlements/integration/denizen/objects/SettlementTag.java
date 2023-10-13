package de.themoep.settlements.integration.denizen.objects;

import com.denizenscript.denizen.objects.LocationTag;
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
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;

public class SettlementTag implements ObjectTag, Adjustable {

    // <--[ObjectType]
    // @name SettlementTag
    // @prefix settlement
    // @base ElementTag
    // @format
    // The identity format for settlements is their name
    // For example, 'settlement@firedev'.
    //
    // @description
    // A SettlementTag represents a Settlement object in Settlements.
    //
    // -->

    //////////////////
    //    Object Fetcher
    ////////////////

    @Fetchable("settlement")
    public static SettlementTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }
        string = CoreUtilities.toLowerCase(string).replace("settlement@", "");
        Settlement settlement = SettlementManager.getInstance().getFromName(string);
        if (settlement != null) {
            return new SettlementTag(settlement);
        }
        return null;
    }

    public static boolean matches(String arg) {
        if (CoreUtilities.toLowerCase(arg).startsWith("settlement@")) {
            return true;
        }
        Settlement settlement = SettlementManager.getInstance().getFromName(arg);
        return settlement != null;
    }

    /////////////////////
    //   Constructors
    //////////////////

    public SettlementTag(Settlement settlement) {
        this.settlement = settlement;
    }

    /////////////////////
    //   Instance Fields/Methods
    /////////////////

    private Settlement settlement;

    public Settlement getSettlement() {
        return settlement;
    }

    /////////////////////
    //  ObjectTag Methods
    ///////////////////

    private String prefix = "Settlement";

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
        return "settlement@" + settlement.getName();
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
    public SettlementTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public static void register() {

        // <--[tag]
        // @attribute <SettlementTag.name>
        // @returns ElementTag
        // @description
        // Gets the settlement's name
        // -->
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> new ElementTag(object.settlement.getName()));

        // <--[tag]
        // @attribute <SettlementTag.leader>
        // @returns ResidentTag
        // @description
        // Gets the settlement's leader
        // -->
        tagProcessor.registerTag(ResidentTag.class, "leader", (attribute, object) -> new ResidentTag(object.settlement.getChief()));

        // <--[tag]
        // @attribute <SettlementTag.online_residents>
        // @returns ListTag(ResidentTag)
        // @description
        // Gets the settlement's online residents
        // -->
        tagProcessor.registerTag(ListTag.class, "online_residents", (attribute, object) -> new ListTag(object.settlement.getOnlineResidents(), ResidentTag::new));

        // <--[tag]
        // @attribute <SettlementTag.residents>
        // @returns ListTag(ResidentTag)
        // @description
        // Gets the settlement's residents
        // -->
        tagProcessor.registerTag(ListTag.class, "residents", (attribute, object) -> new ListTag(object.settlement.getResidents(), ResidentTag::new));

        // <--[tag]
        // @attribute <SettlementTag.state>
        // @returns ElementTag
        // @description
        // Gets the settlement's state
        // -->
        tagProcessor.registerTag(ElementTag.class, "state", (attribute, object) -> new ElementTag(object.settlement.getStateTag()));

        // <--[tag]
        // @attribute <SettlementTag.level>
        // @returns ElementTag
        // @description
        // Gets the settlement's level
        // -->
        tagProcessor.registerTag(ElementTag.class, "level", (attribute, object) -> new ElementTag(object.settlement.getLevel()));

        // <--[tag]
        // @attribute <SettlementTag.balance>
        // @returns ElementTag
        // @description
        // Gets the settlement's bank balance
        // -->
        tagProcessor.registerTag(ElementTag.class, "balance", (attribute, object) -> new ElementTag(object.settlement.getBank()));

        // <--[tag]
        // @attribute <SettlementTag.tax>
        // @returns ElementTag
        // @description
        // Gets the settlement's tax
        // -->
        tagProcessor.registerTag(ElementTag.class, "tax", (attribute, object) -> new ElementTag(object.settlement.getTax()));

        // <--[tag]
        // @attribute <SettlementTag.empire>
        // @returns EmpireTag
        // @description
        // Gets the settlement's empire
        // -->
        tagProcessor.registerTag(EmpireTag.class, "empire", (attribute, object) -> new EmpireTag(object.settlement.getEmpire()));

        // <--[tag]
        // @attribute <SettlementTag.home>
        // @returns LocationTag
        // @description
        // Gets the settlement's home location
        // -->
        tagProcessor.registerTag(LocationTag.class, "home", (attribute, object) -> new LocationTag(object.settlement.getHome().toBukkitLocation()));

        // <--[tag]
        // @attribute <SettlementTag.claimable_land>
        // @returns ElementTag
        // @description
        // Gets the settlement's total claimable land
        // -->
        tagProcessor.registerTag(ElementTag.class, "claimable_land", (attribute, object) -> new ElementTag(object.settlement.getMaxClaimableChunks()));

        // <--[tag]
        // @attribute <SettlementTag.claimed_land>
        // @returns ListTag(LandTag)
        // @description
        // Gets the settlement's claimed land
        // -->
        tagProcessor.registerTag(ListTag.class, "claimed_land", (attribute, object) -> new ListTag(object.settlement.getLand(), LandTag::new));

        // <--[tag]
        // @attribute <SettlementTag.claimable_outposts>
        // @returns ElementTag
        // @description
        // Gets the settlement's total claimable outposts
        // -->
        tagProcessor.registerTag(ElementTag.class, "claimable_outposts", (attribute, object) -> new ElementTag(object.settlement.getMaxOutposts()));

        // <--[tag]
        // @attribute <SettlementTag.claimed_outposts>
        // @returns ListTag(LandTag)
        // @description
        // Gets the settlement's claimed outposts
        // -->
        tagProcessor.registerTag(ListTag.class, "claimed_outposts", (attribute, object) -> new ListTag(object.settlement.getOutpostHomes().values(), namedLocation -> new LocationTag(namedLocation.toBukkitLocation())));

        // <--[tag]
        // @attribute <SettlementTag.upkeep>
        // @returns ElementTag
        // @description
        // Gets the settlement's upkeep cost
        // -->
        tagProcessor.registerTag(ElementTag.class, "upkeep", (attribute, object) -> new ElementTag(object.settlement.getUpkeep()));

        // <--[tag]
        // @attribute <SettlementTag.is_resident[ResidentTag]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the resident is a member of this settlement
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_resident", (attribute, object) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("The tag ResidentTag.is_resident[...] must have a value.");
                return null;
            }
            if (!(attribute.getParamObject() instanceof ResidentTag residentTag)) {
                attribute.echoError("The tag SettlementTag.is_resident[...]'s value must be a ResidentTag.");
                return null;
            }
            return new ElementTag(object.getSettlement().getResidents().contains(residentTag.getResident()));
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object SettlementTag
        // @name set_balance
        // @input ElementTag
        // @description
        // Sets the settlement's Bank Balance
        // @tags
        // <SettlementTag.balance>
        // @example
        // - adjust <settlement[firedev]> set_balance:10000
        // -->
        if (mechanism.matches("set_balance") && mechanism.requireInteger()) {
            settlement.setBank(mechanism.getValue().asLong());
            settlement.save();
        }

        // <--[mechanism]
        // @object SettlementTag
        // @name add_balance
        // @input ElementTag
        // @description
        // Adds to the settlement's Bank Balance
        // @tags
        // <SettlementTag.balance>
        // @example
        // - adjust <settlement[firedev]> add_balance:10000
        // -->
        if (mechanism.matches("add_balance") && mechanism.requireInteger()) {
            settlement.addToBank(mechanism.getValue().asLong());
            settlement.save();
        }

        // <--[mechanism]
        // @object SettlementTag
        // @name remove_balance
        // @input ElementTag
        // @description
        // Takes from the settlement's Bank Balance
        // @tags
        // <SettlementTag.balance>
        // @example
        // - adjust <settlement[firedev]> remove_balance:10000
        // -->
        if (mechanism.matches("remove_balance") && mechanism.requireInteger()) {
            settlement.removeFromBank(mechanism.getValue().asLong());
            settlement.save();
        }
    }

    public static ObjectTagProcessor<SettlementTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        mechanism.echoError("Cannot apply properties to an settlement!");
    }
}