package de.themoep.settlements.integration.denizen.objects;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.objects.WorldTag;
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
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;

import java.util.ArrayList;
import java.util.List;

public class EmpireTag implements ObjectTag, Adjustable {

    public static ObjectTagProcessor<EmpireTag> tagProcessor = new ObjectTagProcessor<>();

    // <--[ObjectType]
    // @name EmpireTag
    // @prefix empire
    // @base ElementTag
    // @format
    // The identity format for empires is their names.
    // For example, 'empire@my_empire'.
    //
    // @plugin Settlements
    // @description
    // An EmpireTag represents an empire.
    //
    // -->

    @Fetchable("empire")
    public static EmpireTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        ////////
        // Match Empire name

        string = string.replace("empire@", "");
        Empire empire = EmpireManager.getInstance().getEmpireFromName(string);
        if (empire == null) {
            return null;
        }
        return new EmpireTag(empire);
    }

    Empire empire = null;

    public EmpireTag(Empire empire) {
        if (empire != null) {
            this.empire = empire;
        }
        else {
            Debug.echoError("Empire referenced is null!");
        }
    }

    public static boolean matches(String arg) {
        if (valueOf(arg, CoreUtilities.noDebugContext) != null) {
            return true;
        }
        return false;
    }

    String prefix = "empire";

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
    public ObjectTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String toString() {
        return identify();
    }

    public Empire getEmpire() {
        return empire;
    }

    public boolean equals(Empire empire1) {
        return empire1.equals(this.empire);
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        mechanism.echoError("Cannot apply Properties to an empire!");
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public static void register() {
        PropertyParser.registerPropertyTagHandlers(EmpireTag.class, tagProcessor);

        // <--[tag]
        // @attribute <EmpireTag.name>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the name of the empire.
        // -->
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> new ElementTag(object.getEmpire().getName()));

        // <--[tag]
        // @attribute <EmpireTag.leader>
        // @returns PlayerTag
        // @plugin Settlements
        // @description
        // Returns the leader of the empire.
        // -->
        tagProcessor.registerTag(ResidentTag.class, "leader", (attribute, object) -> new ResidentTag(object.getEmpire().getCapital().getChief()));

        // <--[tag]
        // @attribute <EmpireTag.capital>
        // @returns SettlementTag
        // @plugin Settlements
        // @description
        // Returns the capital of the empire.
        // -->
        tagProcessor.registerTag(SettlementTag.class, "capital", (attribute, object) -> new SettlementTag(object.getEmpire().getCapital()));

        // <--[tag]
        // @attribute <EmpireTag.online_residents>
        // @returns ListTag(PlayerTag)
        // @plugin Settlements
        // @description
        // Returns the online residents of the empire.
        // -->
        tagProcessor.registerTag(ListTag.class, "online_residents", (attribute, object) -> {
            List<PlayerTag> residents = new ArrayList<>();
            for (Resident resident : object.getEmpire().getOnlineResidents()) {
                residents.add(new PlayerTag(resident.getUUID()));
            }
            return new ListTag(residents);
        });

        // <--[tag]
        // @attribute <EmpireTag.residents>
        // @returns ListTag(PlayerTag)
        // @plugin Settlements
        // @description
        // Returns the residents of the empire.
        // -->
        tagProcessor.registerTag(ListTag.class, "residents", (attribute, object) -> {
            List<PlayerTag> residents = new ArrayList<>();
            for (Resident resident : object.getEmpire().getResidents()) {
                residents.add(new PlayerTag(resident.getUUID()));
            }
            return new ListTag(residents);
        });

        // <--[tag]
        // @attribute <EmpireTag.members>
        // @returns ListTag(EmpireTag)
        // @plugin Settlements
        // @description
        // Returns the member settlements of the empire.
        // -->
        tagProcessor.registerTag(ListTag.class, "members", (attribute, object) -> {
            List<SettlementTag> members = new ArrayList<>();
            for (Settlement settlement : object.getEmpire().getMembers()) {
                members.add(new SettlementTag(settlement));
            }
            return new ListTag(members);
        });

        // <--[tag]
        // @attribute <EmpireTag.level>
        // @returns ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Returns the level of the empire.
        // -->
        tagProcessor.registerTag(ElementTag.class, "level", (attribute, object) -> new ElementTag(object.getEmpire().getLevel()));

        // <--[tag]
        // @attribute <EmpireTag.balance>
        // @returns ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Returns the balance of the empire.
        // -->
        tagProcessor.registerTag(ElementTag.class, "balance", (attribute, object) -> new ElementTag(object.getEmpire().getBank()));

        // <--[tag]
        // @attribute <EmpireTag.tax>
        // @returns ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Returns the tax of the empire.
        // -->
        tagProcessor.registerTag(ElementTag.class, "tax", (attribute, object) -> new ElementTag(object.getEmpire().getTax()));

        // <--[tag]
        // @attribute <EmpireTag.world>
        // @returns WorldTag
        // @plugin Settlements
        // @description
        // Returns the world of the empire.
        // -->
        tagProcessor.registerTag(WorldTag.class, "world", (attribute, object) -> {
            if (!object.getEmpire().hasWorld()) {
                return null;
            }
            return new WorldTag(object.getEmpire().getWorldName());
        });

        // <--[tag]
        // @attribute <EmpireTag.home>
        // @returns LocationTag
        // @plugin Settlements
        // @description
        // Returns the home of the empire.
        // -->
        tagProcessor.registerTag(LocationTag.class, "home", (attribute, object) -> {
            if (!object.getEmpire().hasHome()) {
                return null;
            }
            return new LocationTag(object.getEmpire().getHome().toBukkitLocation());
        });

        // <--[tag]
        // @attribute <EmpireTag.upkeep>
        // @returns ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Returns the upkeep of the empire.
        // -->
        tagProcessor.registerTag(ElementTag.class, "upkeep", (attribute, object) -> new ElementTag(object.getEmpire().getUpkeep()));
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EmpireTag
        // @name balance
        // @input ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Sets the money balance of a empire.
        // @tags
        // <EmpireTag.balance>
        // -->
        if (mechanism.matches("balance")) {
            empire.setBank(mechanism.getValue().asLong());
            DatabaseManager.getInstance().saveEmpire(empire);
        }

        // <--[mechanism]
        // @object EmpireTag
        // @name add_balance
        // @input ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Adds to the money balance of a empire.
        // @tags
        // <EmpireTag.add_balance>
        // -->
        if (mechanism.matches("add_balance")) {
            empire.addToBank(mechanism.getValue().asLong());
            DatabaseManager.getInstance().saveEmpire(empire);
        }

        // <--[mechanism]
        // @object EmpireTag
        // @name remove_balance
        // @input ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Takes from the money balance of a empire.
        // @tags
        // <EmpireTag.remove_balance>
        // -->
        if (mechanism.matches("remove_balance")) {
            empire.removeFromBank(mechanism.getValue().asLong());
            DatabaseManager.getInstance().saveEmpire(empire);
        }

        // <--[mechanism]
        // @object EmpireTag
        // @name add_member
        // @input ElementTag(String)
        // @plugin Settlements
        // @description
        // Add a member to this empire.
        // @tags
        // <EmpireTag.add_member>
        // -->
        if (mechanism.matches("add_member")) {
            Settlement settlement = SettlementManager.getInstance().getFromName(mechanism.getValue().asString());
            if (settlement == null) {
                mechanism.echoError("No settlement found for '" + mechanism.getValue().asString() + "'");
                return;
            }
            empire.addMember(settlement);
            DatabaseManager.getInstance().saveEmpire(empire);
        }

        // <--[mechanism]
        // @object EmpireTag
        // @name remove_member
        // @input ElementTag(String)
        // @plugin Settlements
        // @description
        // Removes a member from this empire.
        // @tags
        // <EmpireTag.remove_member>
        // -->
        if (mechanism.matches("remove_member")) {
            Settlement settlement = SettlementManager.getInstance().getFromName(mechanism.getValue().asString());
            if (settlement == null) {
                mechanism.echoError("No settlement found for '" + mechanism.getValue().asString() + "'");
                return;
            }
            empire.removeMember(settlement);
            DatabaseManager.getInstance().saveEmpire(empire);
        }

        tagProcessor.processMechanism(this, mechanism);
        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }
}