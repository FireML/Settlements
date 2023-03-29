package de.themoep.settlements.integration.denizen.objects;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.objects.WorldTag;
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
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;

import java.util.ArrayList;
import java.util.List;

public class EmpireTag implements ObjectTag {

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
    public ObjectTag getObjectAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EmpireTag.name>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the name of the empire.
        // -->
        if (attribute.startsWith("name")) {
            return new ElementTag(empire.getName());
        }

        // <--[tag]
        // @attribute <EmpireTag.leader>
        // @returns PlayerTag
        // @plugin Settlements
        // @description
        // Returns the leader of the empire.
        // -->
        if (attribute.startsWith("leader")) {
            return new PlayerTag(empire.getCapital().getChief().getPlayer());
        }

        // <--[tag]
        // @attribute <EmpireTag.capital>
        // @returns SettlementTag
        // @plugin Settlements
        // @description
        // Returns the capital of the empire.
        // -->
        if (attribute.startsWith("capital")) {
            return new SettlementTag(empire.getCapital());
        }

        // <--[tag]
        // @attribute <EmpireTag.online_residents>
        // @returns ListTag(PlayerTag)
        // @plugin Settlements
        // @description
        // Returns the online residents of the empire.
        // -->
        if (attribute.startsWith("online_residents")) {
            List<PlayerTag> residents = new ArrayList<>();
            for (Resident resident : empire.getOnlineResidents()) {
                residents.add(new PlayerTag(resident.getPlayer()));
            }
            return new ListTag(residents);
        }

        // <--[tag]
        // @attribute <EmpireTag.residents>
        // @returns ListTag(PlayerTag)
        // @plugin Settlements
        // @description
        // Returns the residents of the empire.
        // -->
        if (attribute.startsWith("residents")) {
            List<PlayerTag> residents = new ArrayList<>();
            for (Resident resident : empire.getResidents()) {
                residents.add(new PlayerTag(resident.getPlayer()));
            }
            return new ListTag(residents);
        }

        // <--[tag]
        // @attribute <EmpireTag.members>
        // @returns ListTag(EmpireTag)
        // @plugin Settlements
        // @description
        // Returns the member settlements of the empire.
        // -->
        if (attribute.startsWith("members")) {
            List<SettlementTag> members = new ArrayList<>();
            for (Settlement settlement : empire.getMembers()) {
                members.add(new SettlementTag(settlement));
            }
            return new ListTag(members);
        }

        // <--[tag]
        // @attribute <EmpireTag.level>
        // @returns ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Returns the level of the empire.
        // -->
        if (attribute.startsWith("level")) {
            return new ElementTag(empire.getLevel());
        }

        // <--[tag]
        // @attribute <EmpireTag.balance>
        // @returns ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Returns the balance of the empire.
        // -->
        if (attribute.startsWith("balance")) {
            return new ElementTag(empire.getBank());
        }

        // <--[tag]
        // @attribute <EmpireTag.tax>
        // @returns ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Returns the tax of the empire.
        // -->
        if (attribute.startsWith("tax")) {
            return new ElementTag(empire.getTax());
        }

        // <--[tag]
        // @attribute <EmpireTag.world>
        // @returns WorldTag
        // @plugin Settlements
        // @description
        // Returns the world of the empire.
        // -->
        if (attribute.startsWith("tax")) {
            if (!empire.hasWorld()) {
                return null;
            }
            return new WorldTag(empire.getWorldName());
        }

        // <--[tag]
        // @attribute <EmpireTag.home>
        // @returns LocationTag
        // @plugin Settlements
        // @description
        // Returns the home of the empire.
        // -->
        if (attribute.startsWith("home")) {
            if (!empire.hasHome()) {
                return null;
            }
            return new LocationTag(empire.getHome().toBukkitLocation());
        }

        // <--[tag]
        // @attribute <EmpireTag.upkeep>
        // @returns ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Returns the upkeep of the empire.
        // -->
        if (attribute.startsWith("upkeep")) {
            return new ElementTag(empire.getUpkeep());
        }

        // <--[tag]
        // @attribute <EmpireTag.upkeep>
        // @returns ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Returns the upkeep of the empire.
        // -->
        if (attribute.startsWith("upkeep")) {
            return new ElementTag(empire.getUpkeep());
        }

        return new ElementTag(identify()).getObjectAttribute(attribute);
    }

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