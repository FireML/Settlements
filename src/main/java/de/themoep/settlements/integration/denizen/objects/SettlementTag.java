package de.themoep.settlements.integration.denizen.objects;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Adjustable;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.Use;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.world.NamedLocation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SettlementTag implements ObjectTag, Adjustable {

    // <--[ObjectType]
    // @name SettlementTag
    // @prefix settlement
    // @base ElementTag
    // @format
    // The identity format for settlements is their name
    // For example, 'settlement@MyTown'.
    //
    // @plugin Settlements
    // @description
    // A SettlementTag represents a settlement (village or kingdom).
    //
    // -->

    public static final ObjectTagProcessor<SettlementTag> tagProcessor = new ObjectTagProcessor<>();

    private static final String PREFIX = "settlement";

    private final Settlement settlement;
    private String prefix = PREFIX;

    public static SettlementTag valueOf(String string) {
        return SettlementTag.valueOf(string, null);
    }

    @Fetchable("settlement")
    public static SettlementTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        string = string.replaceFirst(PREFIX.toLowerCase(Locale.ROOT) + "@", "");

        Settlement settlement = SettlementManager.getInstance().getFromName(string);
        if (settlement == null) {
            return null;
        }

        return new SettlementTag(settlement);
    }

    public static boolean matches(String arg) {
        arg = arg.replaceFirst(PREFIX.toLowerCase(Locale.ROOT) + "@", "");
        return SettlementManager.getInstance().getFromName(arg) != null;
    }

    public SettlementTag(Settlement settlement) {
        this.settlement = settlement;
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
        return PREFIX.toLowerCase(Locale.ROOT) + "@" + settlement.getName();
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

    public Settlement getSettlement() {
        return settlement;
    }

    public boolean equals(SettlementTag residentTag) {
        return residentTag.getSettlement().getId().equals(getSettlement().getId());
    }

    public static void registerTags() {
        // <--[tag]
        // @attribute <settlement.name>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the name of the Settlement.
        // -->
        tagProcessor.registerTag(ElementTag.class, "name",
                (attribute, object) -> new ElementTag(object.getSettlement().getName()));

        // <--[tag]
        // @attribute <settlement.leader>
        // @returns ResidentTag
        // @plugin Settlements
        // @description
        // Returns the leader of this settlement.
        // -->
        tagProcessor.registerTag(ResidentTag.class, "leader",
                (attribute, object) -> new ResidentTag(object.getSettlement().getChief()));

        // <--[tag]
        // @attribute <settlement.online_residents>
        // @returns ListTag(PlayerTag)
        // @plugin Settlements
        // @description
        // Returns the online residents of the Settlement.
        // -->
        tagProcessor.registerTag(ListTag.class, "online_residents", (attribute, object) -> {
            List<PlayerTag> members = new ArrayList<>();
            for (UUID residentId : object.getSettlement().getResidentIds()) {
                Player player = Bukkit.getPlayer(residentId);
                if (player != null) {
                    members.add(new PlayerTag(player));
                }
            }
            return new ListTag(members);
        });

        // <--[tag]
        // @attribute <settlement.online_residents>
        // @returns ListTag(PlayerTag)
        // @plugin Settlements
        // @description
        // Returns the online residents of the Settlement.
        // -->
        tagProcessor.registerTag(ListTag.class, "online_residents", (attribute, object) -> {
            List<PlayerTag> members = new ArrayList<>();
            for (UUID residentId : object.getSettlement().getResidentIds()) {
                Player player = Bukkit.getPlayer(residentId);
                if (player != null) {
                    members.add(new PlayerTag(residentId));
                }
            }
            return new ListTag(members);
        });

        // <--[tag]
        // @attribute <settlement.residents>
        // @returns ListTag(PlayerTag)
        // @plugin Settlements
        // @description
        // Returns the residents of the Settlement.
        // -->
        tagProcessor.registerTag(ListTag.class, "residents", (attribute, object) -> {
            List<PlayerTag> members = new ArrayList<>();
            for (UUID residentId : object.getSettlement().getResidentIds()) {
                members.add(new PlayerTag(residentId));
            }
            return new ListTag(members);
        });

        // <--[tag]
        // @attribute <settlement.state>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the state of the Settlement.
        // -->
        tagProcessor.registerTag(ElementTag.class, "state",
                (attribute, object) -> new ElementTag(object.getSettlement().getStateTag()));

        // <--[tag]
        // @attribute <settlement.level>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the level of the Settlement.
        // -->
        tagProcessor.registerTag(ElementTag.class, "level",
                (attribute, object) -> new ElementTag(object.getSettlement().getLevel()));

        // <--[tag]
        // @attribute <settlement.balance>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the balance of the Settlement.
        // -->
        tagProcessor.registerTag(ElementTag.class, "balance",
                (attribute, object) -> new ElementTag(object.getSettlement().getBank()));

        // <--[tag]
        // @attribute <settlement.tax>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the tax of the Settlement.
        // -->
        tagProcessor.registerTag(ElementTag.class, "tax",
                (attribute, object) -> new ElementTag(object.getSettlement().getTax()));

        // <--[tag]
        // @attribute <settlement.empire>
        // @returns EmpireTag
        // @plugin Settlements
        // @description
        // Returns the empire that a settlement belongs to.
        // -->
        tagProcessor.registerTag(EmpireTag.class, "empire", (attribute, object) -> {
            if (!object.getSettlement().hasEmpire()) {
                return null;
            }

            return new EmpireTag(object.getSettlement().getEmpire());
        });

        // <--[tag]
        // @attribute <settlement.home>
        // @returns LocationTag
        // @plugin Settlements
        // @description
        // Returns the home of this settlement.
        // -->
        tagProcessor.registerTag(LocationTag.class, "home", (attribute, object) -> {
            if (object.getSettlement().getHome() == null) {
                return null;
            }

            return new LocationTag(object.getSettlement().getHome().toBukkitLocation());
        });

        // <--[tag]
        // @attribute <settlement.claimable_land>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the amount of land the Settlement can claim.
        // -->
        tagProcessor.registerTag(ElementTag.class, "claimable_land",
                (attribute, object) -> new ElementTag(object.getSettlement().getMaxClaimableChunks()));

        // <--[tag]
        // @attribute <settlement.claimable_outposts>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the amount of outposts the Settlement can claim.
        // -->
        tagProcessor.registerTag(ElementTag.class, "claimable_outposts",
                (attribute, object) -> new ElementTag(object.getSettlement().getMaxOutposts()));

        // <--[tag]
        // @attribute <settlement.upkeep>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the upkeep of the Settlement.
        // -->
        tagProcessor.registerTag(ElementTag.class, "upkeep",
                (attribute, object) -> new ElementTag(object.getSettlement().getUpkeep()));

        // <--[tag]
        // @attribute <settlement.outposts>
        // @returns ListTag(LocationTag)
        // @plugin Settlements
        // @description
        // Returns the home of this settlement.
        // -->
        tagProcessor.registerTag(ListTag.class, "outposts", (attribute, object) -> {
            List<LocationTag> outposts = new ArrayList<>();
            for (NamedLocation location : object.getSettlement().getOutpostHomes().values()) {
                outposts.add(new LocationTag(location.toBukkitLocation()));
            }
            return new ListTag(outposts);
        });

        // <--[tag]
        // @attribute <settlement.claimed_land>
        // @returns ListTag(LandTag)
        // @plugin Settlements
        // @description
        // Returns the land claimed by this settlement.
        // -->
        tagProcessor.registerTag(ListTag.class, "claimed_land", (attribute, object) -> {
            List<LandTag> lands = new ArrayList<>();
            for (OwnedLand land : object.getSettlement().getLand()) {
                lands.add(new LandTag(land));
            }
            return new ListTag(lands);
        });

        for (PlotMode mode : PlotMode.values()) {
            // <--[tag]
            // @attribute <settlement.mode_name>
            // @returns ElementTag
            // @plugin Settlements
            // @description
            // Returns whether the mode is enabled for the settlement.
            // -->
            tagProcessor.registerTag(ElementTag.class, "mode_" + mode.name().toLowerCase(Locale.ROOT),
                    (attribute, object) -> new ElementTag(object.getSettlement().getMode(mode)));
        }

        for (Use use : Use.values()) {
            // <--[tag]
            // @attribute <settlement.use_name>
            // @returns ElementTag
            // @plugin Settlements
            // @description
            // Returns the use flag state of the settlement.
            // -->
            tagProcessor.registerTag(ElementTag.class, "use_" + use.name().toLowerCase(Locale.ROOT),
                    (attribute, object) -> new ElementTag(object.getSettlement().getUseState(use).name().toLowerCase(Locale.ROOT)));
        }
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object SettlementTag
        // @name balance
        // @input SettlementTag(Decimal)
        // @plugin Settlements
        // @description
        // Sets the money balance of a settlement.
        // @tags
        // <SettlementTag.balance>
        // -->
        if (mechanism.matches("balance")) {
            settlement.setBank(mechanism.getValue().asLong());
            DatabaseManager.getInstance().saveSettlement(settlement);
            return;
        }

        // <--[mechanism]
        // @object SettlementTag
        // @name add_balance
        // @input SettlementTag(Decimal)
        // @plugin Settlements
        // @description
        // Adds to the money balance of a settlement.
        // @tags
        // <SettlementTag.balance>
        // -->
        if (mechanism.matches("add_balance")) {
            settlement.addToBank(mechanism.getValue().asLong());
            DatabaseManager.getInstance().saveSettlement(settlement);
            return;
        }

        // <--[mechanism]
        // @object SettlementTag
        // @name add_balance
        // @input SettlementTag(Decimal)
        // @plugin Settlements
        // @description
        // Adds to the money balance of a settlement.
        // @tags
        // <SettlementTag.balance>
        // -->
        if (mechanism.matches("remove_balance")) {
            settlement.removeFromBank(mechanism.getValue().asLong());
            DatabaseManager.getInstance().saveSettlement(settlement);
            return;
        }

        // <--[mechanism]
        // @object SettlementTag
        // @name add_member
        // @input ElementTag(String)
        // @plugin Settlements
        // @description
        // Add a member to this empire
        // @tags
        // <SettlementTag.add_member>
        // -->
        if (mechanism.matches("add_member")) {
            String name = mechanism.getValue().asString();
            Resident resident = ResidentManager.getInstance().getResident(name);
            if (resident == null) {
                mechanism.echoError("No resident found for '" + name + "'");
                return;
            }

            settlement.addMember(resident);
            return;
        }

        // <--[mechanism]
        // @object EmpireTag
        // @name remove_member
        // @input ElementTag(String)
        // @plugin Settlements
        // @description
        // Remove a member from this empire
        // @tags
        // <EmpireTag.remove_member>
        // -->
        if (mechanism.matches("remove_member")) {
            String name = mechanism.getValue().asString();
            Resident resident = ResidentManager.getInstance().getResident(name);
            if (resident == null || !settlement.isMember(resident)) {
                mechanism.echoError("No member found for '" + name + "'");
                return;
            }

            resident.leaveSettlement();
        }
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        Debug.echoError("Cannot apply properties to a settlement!");
    }
}
