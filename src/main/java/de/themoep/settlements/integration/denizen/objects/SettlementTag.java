package de.themoep.settlements.integration.denizen.objects;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
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
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.world.NamedLocation;

import java.util.ArrayList;
import java.util.List;

public class SettlementTag implements ObjectTag {

    public static ObjectTagProcessor<SettlementTag> tagProcessor = new ObjectTagProcessor<>();

    // <--[ObjectType]
    // @name SettlementTag
    // @prefix settlement
    // @base ElementTag
    // @format
    // The identity format for settlements is their names.
    // For example, 'settlement@my_settlement'.
    //
    // @plugin Settlements
    // @description
    // A SettlementTag represents a settlement.
    //
    // -->

    @Fetchable("settlement")
    public static SettlementTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        ////////
        // Match Settlement name

        string = string.replace("settlement@", "");
        Settlement settlement = SettlementManager.getInstance().getFromName(string);
        if (settlement == null) {
            return null;
        }
        return new SettlementTag(settlement);
    }

    Settlement settlement  = null;

    public SettlementTag(Settlement settlement) {
        if (settlement != null) {
            this.settlement = settlement;
        }
        else {
            Debug.echoError("Settlement referenced is null!");
        }
    }

    String prefix = "settlement";

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
    public ObjectTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String toString() {
        return identify();
    }

    public Settlement getSettlement() {
        return settlement;
    }

    public boolean equals(Settlement settlement1) {
        return settlement1.equals(this.settlement);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <SettlementTag.name>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the name of the settlement.
        // -->
        if (attribute.startsWith("name")) {
            return new ElementTag(settlement.getName());
        }

        // <--[tag]
        // @attribute <SettlementTag.leader>
        // @returns PlayerTag
        // @plugin Settlements
        // @description
        // Returns the leader of the settlement.
        // -->
        if (attribute.startsWith("leader")) {
            return new PlayerTag(settlement.getChief().getPlayer());
        }

        // <--[tag]
        // @attribute <SettlementTag.online_residents>
        // @returns ListTag(PlayerTag)
        // @plugin Settlements
        // @description
        // Returns the online residents of the settlement.
        // -->
        if (attribute.startsWith("online_residents")) {
            List<PlayerTag> residents = new ArrayList<>();
            for (Resident resident : settlement.getOnlineResidents()) {
                residents.add(new PlayerTag(resident.getPlayer().getUniqueId()));
            }
            return new ListTag(residents);
        }

        // <--[tag]
        // @attribute <SettlementTag.residents>
        // @returns ListTag(PlayerTag)
        // @plugin Settlements
        // @description
        // Returns the residents of the settlement.
        // -->
        if (attribute.startsWith("residents")) {
            List<PlayerTag> residents = new ArrayList<>();
            for (Resident resident : settlement.getResidents()) {
                residents.add(new PlayerTag(resident.getPlayer().getUniqueId()));
            }
            return new ListTag(residents);
        }

        // <--[tag]
        // @attribute <SettlementTag.state>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the state of the settlement.
        // -->
        if (attribute.startsWith("state")) {
            return new ElementTag(settlement.getStateTag());
        }

        // <--[tag]
        // @attribute <SettlementTag.level>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the level of the settlement.
        // -->
        if (attribute.startsWith("level")) {
            return new ElementTag(settlement.getLevel());
        }

        // <--[tag]
        // @attribute <SettlementTag.balance>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the balance of the settlement.
        // -->
        if (attribute.startsWith("balance")) {
            return new ElementTag(settlement.getBank());
        }

        // <--[tag]
        // @attribute <SettlementTag.tax>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the tax of the settlement.
        // -->
        if (attribute.startsWith("tax")) {
            return new ElementTag(settlement.getTax());
        }

        // <--[tag]
        // @attribute <SettlementTag.empire>
        // @returns EmpireTag
        // @plugin Settlements
        // @description
        // Returns the empire that the settlement belongs to.
        // -->
        if (attribute.startsWith("empire")) {
            if (!settlement.hasEmpire()) {
                return null;
            }
            return new EmpireTag(settlement.getEmpire());
        }

        // <--[tag]
        // @attribute <SettlementTag.home>
        // @returns LocationTag
        // @plugin Settlements
        // @description
        // Returns the home of this settlement.
        // -->
        if (attribute.startsWith("home")) {
            if (!settlement.hasHome()) {
                return null;
            }
            return new LocationTag(settlement.getHome().toBukkitLocation());
        }

        // <--[tag]
        // @attribute <SettlementTag.claimable_land>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the amount of land the settlement can claim.
        // -->
        if (attribute.startsWith("claimable_land")) {
            return new ElementTag(settlement.getMaxClaimableChunks());
        }

        // <--[tag]
        // @attribute <SettlementTag.claimable_outposts>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the amount of outposts the settlement can claim.
        // -->
        if (attribute.startsWith("claimable_outposts")) {
            return new ElementTag(settlement.getMaxOutposts());
        }

        // <--[tag]
        // @attribute <SettlementTag.upkeep>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the upkeep of the settlement.
        // -->
        if (attribute.startsWith("upkeep")) {
            return new ElementTag(settlement.getUpkeep());
        }

        // <--[tag]
        // @attribute <SettlementTag.claimed_outposts>
        // @returns ListTag(LocationTag)
        // @plugin Settlements
        // @description
        // Returns the claimed outposts of the settlement.
        // -->
        if (attribute.startsWith("claimed_outposts")) {
            List<LocationTag> outposts = new ArrayList<>();
            for (NamedLocation location : settlement.getOutpostHomes().values()) {
                outposts.add(new LocationTag(location.toBukkitLocation()));
            }
            return new ListTag(outposts);
        }

        // <--[tag]
        // @attribute <SettlementTag.claimed_land>
        // @returns ListTag(LandTag)
        // @plugin Settlements
        // @description
        // Returns the land claimed by the settlement.
        // -->
        if (attribute.startsWith("claimed_land")) {
            List<LandTag> lands = new ArrayList<>();
            for (OwnedLand land : settlement.getLand()) {
                lands.add(new LandTag(land));
            }
            return new ListTag(lands);
        }

        return new ElementTag(identify()).getObjectAttribute(attribute);
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object SettlementTag
        // @name balance
        // @input ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Sets the money balance of a settlement.
        // @tags
        // <SettlementTag.balance>
        // -->
        if (mechanism.matches("balance")) {
            settlement.setBank(mechanism.getValue().asLong());
            DatabaseManager.getInstance().saveSettlement(settlement);
        }

        // <--[mechanism]
        // @object SettlementTag
        // @name add_balance
        // @input ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Adds to the money balance of a settlement.
        // @tags
        // <SettlementTag.add_balance>
        // -->
        if (mechanism.matches("add_balance")) {
            settlement.addToBank(mechanism.getValue().asLong());
            DatabaseManager.getInstance().saveSettlement(settlement);
        }

        // <--[mechanism]
        // @object SettlementTag
        // @name remove_balance
        // @input ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Takes from the money balance of a settlement.
        // @tags
        // <SettlementTag.remove_balance>
        // -->
        if (mechanism.matches("remove_balance")) {
            settlement.removeFromBank(mechanism.getValue().asLong());
            DatabaseManager.getInstance().saveSettlement(settlement);
        }

        // <--[mechanism]
        // @object SettlementTag
        // @name add_member
        // @input ElementTag(String)
        // @plugin Settlements
        // @description
        // Add a member to this settlement.
        // @tags
        // <SettlementTag.add_member>
        // -->
        if (mechanism.matches("add_member")) {
            Resident resident = ResidentManager.getInstance().getResident(mechanism.getValue().asString());
            if (resident == null) {
                mechanism.echoError("No resident found for '" + mechanism.getValue().asString() + "'");
                return;
            }
            settlement.addMember(resident);
            DatabaseManager.getInstance().saveSettlement(settlement);
        }

        // <--[mechanism]
        // @object SettlementTag
        // @name remove_member
        // @input ElementTag(String)
        // @plugin Settlements
        // @description
        // Removes a member from this settlement.
        // @tags
        // <SettlementTag.remove_member>
        // -->
        if (mechanism.matches("remove_member")) {
            Resident resident = ResidentManager.getInstance().getResident(mechanism.getValue().asString());
            if (resident == null) {
                mechanism.echoError("No resident found for '" + mechanism.getValue().asString() + "'");
                return;
            }
            settlement.removeMember(resident);
            DatabaseManager.getInstance().saveSettlement(settlement);
        }

        tagProcessor.processMechanism(this, mechanism);
        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }
}