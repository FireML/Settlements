package de.themoep.settlements.integration.denizen.objects;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.WorldTag;
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
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;

public class EmpireTag implements ObjectTag, Adjustable {

    // <--[ObjectType]
    // @name EmpireTag
    // @prefix empire
    // @base ElementTag
    // @format
    // The identity format for empires is their name
    // For example, 'empire@firedev'.
    //
    // @description
    // A EmpireTag represents an Empire object in Settlements.
    //
    // -->

    //////////////////
    //    Object Fetcher
    ////////////////

    @Fetchable("empire")
    public static EmpireTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }
        string = CoreUtilities.toLowerCase(string).replace("empire@", "");
        Empire empire = EmpireManager.getInstance().getEmpireFromName(string);
        if (empire != null) {
            return new EmpireTag(empire);
        }
        return null;
    }

    public static boolean matches(String arg) {
        if (CoreUtilities.toLowerCase(arg).startsWith("empire@")) {
            return true;
        }
        Empire empire = EmpireManager.getInstance().getEmpireFromName(arg);
        return empire != null;
    }

    /////////////////////
    //   Constructors
    //////////////////

    public EmpireTag(Empire empire) {
        this.empire = empire;
    }

    /////////////////////
    //   Instance Fields/Methods
    /////////////////

    private Empire empire;

    public Empire getEmpire() {
        return empire;
    }

    /////////////////////
    //  ObjectTag Methods
    ///////////////////

    private String prefix = "Empire";

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
        return "empire@" + empire.getName();
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
    public EmpireTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public static void register() {

        // <--[tag]
        // @attribute <EmpireTag.name>
        // @returns ElementTag
        // @description
        // Gets the empire's name
        // -->
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> new ElementTag(object.empire.getName()));

        // <--[tag]
        // @attribute <EmpireTag.leader>
        // @returns ResidentTag
        // @description
        // Gets the empire's leader
        // -->
        tagProcessor.registerTag(ResidentTag.class, "leader", (attribute, object) -> new ResidentTag(object.empire.getChief()));

        // <--[tag]
        // @attribute <EmpireTag.capital>
        // @returns SettlementTag
        // @description
        // Gets the empire's capital
        // -->
        tagProcessor.registerTag(SettlementTag.class, "capital", (attribute, object) -> new SettlementTag(object.empire.getCapital()));

        // <--[tag]
        // @attribute <EmpireTag.online_residents>
        // @returns ListTag(ResidentTag)
        // @description
        // Gets the empire's online residents
        // -->
        tagProcessor.registerTag(ListTag.class, "online_residents", (attribute, object) -> new ListTag(object.empire.getOnlineResidents(), ResidentTag::new));

        // <--[tag]
        // @attribute <EmpireTag.residents>
        // @returns ListTag(ResidentTag)
        // @description
        // Gets the empire's residents
        // -->
        tagProcessor.registerTag(ListTag.class, "residents", (attribute, object) -> new ListTag(object.empire.getResidents(), ResidentTag::new));

        // <--[tag]
        // @attribute <EmpireTag.members>
        // @returns ListTag(SettlementTag)
        // @description
        // Gets the empire's member settlements
        // -->
        tagProcessor.registerTag(ListTag.class, "members", (attribute, object) -> new ListTag(object.empire.getMembers(), SettlementTag::new));

        // <--[tag]
        // @attribute <EmpireTag.level>
        // @returns ElementTag
        // @description
        // Gets the empire's level
        // -->
        tagProcessor.registerTag(ElementTag.class, "level", (attribute, object) -> new ElementTag(object.empire.getLevel()));

        // <--[tag]
        // @attribute <EmpireTag.balance>
        // @returns ElementTag
        // @description
        // Gets the empire's bank balance
        // -->
        tagProcessor.registerTag(ElementTag.class, "balance", (attribute, object) -> new ElementTag(object.empire.getBank()));

        // <--[tag]
        // @attribute <EmpireTag.tax>
        // @returns ElementTag
        // @description
        // Gets the empire's tax
        // -->
        tagProcessor.registerTag(ElementTag.class, "tax", (attribute, object) -> new ElementTag(object.empire.getTax()));

        // <--[tag]
        // @attribute <EmpireTag.world>
        // @returns WorldTag
        // @description
        // Gets the empire's level
        // -->
        tagProcessor.registerTag(WorldTag.class, "world", (attribute, object) -> new WorldTag(object.empire.getWorld()));

        // <--[tag]
        // @attribute <EmpireTag.home>
        // @returns LocationTag
        // @description
        // Gets the empire's home location
        // -->
        tagProcessor.registerTag(LocationTag.class, "home", (attribute, object) -> new LocationTag(object.empire.getHome().toBukkitLocation()));

        // <--[tag]
        // @attribute <EmpireTag.upkeep>
        // @returns ElementTag
        // @description
        // Gets the empire's upkeep cost
        // -->
        tagProcessor.registerTag(ElementTag.class, "upkeep", (attribute, object) -> new ElementTag(object.empire.getUpkeep()));
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EmpireTag
        // @name set_balance
        // @input ElementTag
        // @description
        // Sets the Empire's Bank Balance
        // @tags
        // <EmpireTag.balance>
        // @example
        // - adjust <empire[firedev]> set_balance:10000
        // -->
        if (mechanism.matches("set_balance") && mechanism.requireInteger()) {
            empire.setBank(mechanism.getValue().asLong());
        }

        // <--[mechanism]
        // @object EmpireTag
        // @name add_balance
        // @input ElementTag
        // @description
        // Adds to the Empire's Bank Balance
        // @tags
        // <EmpireTag.balance>
        // @example
        // - adjust <empire[firedev]> add_balance:10000
        // -->
        if (mechanism.matches("add_balance") && mechanism.requireInteger()) {
            empire.addToBank(mechanism.getValue().asLong());
        }

        // <--[mechanism]
        // @object EmpireTag
        // @name remove_balance
        // @input ElementTag
        // @description
        // Takes from the Empire's Bank Balance
        // @tags
        // <EmpireTag.balance>
        // @example
        // - adjust <empire[firedev]> remove_balance:10000
        // -->
        if (mechanism.matches("remove_balance") && mechanism.requireInteger()) {
            empire.removeFromBank(mechanism.getValue().asLong());
        }
    }

    public static ObjectTagProcessor<EmpireTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        mechanism.echoError("Cannot apply properties to an empire!");
    }
}