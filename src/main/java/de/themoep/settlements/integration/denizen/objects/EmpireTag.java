package de.themoep.settlements.integration.denizen.objects;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Adjustable;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.InviteCache;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.Use;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class EmpireTag implements ObjectTag, Adjustable {

    // <--[ObjectType]
    // @name EmpireTag
    // @prefix empire
    // @base ElementTag
    // @format
    // The identity format for empire is their name
    // For example, 'empire@CoolEmpire'.
    //
    // @plugin Settlements
    // @description
    // An EmpireTag represents an empire.
    //
    // -->

    public static final ObjectTagProcessor<EmpireTag> tagProcessor = new ObjectTagProcessor<>();

    private static final String PREFIX = "Empire";

    private final Empire empire;
    private String prefix = PREFIX;

    public static EmpireTag valueOf(String string) {
        return EmpireTag.valueOf(string, null);
    }

    @Fetchable("settlement")
    public static EmpireTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        string = string.replaceFirst(PREFIX.toLowerCase(Locale.ROOT) + "@", "");

        Empire empire = EmpireManager.getInstance().getEmpireFromName(string);
        if (empire == null) {
            return null;
        }

        return new EmpireTag(empire);
    }

    public static boolean matches(String arg) {
        arg = arg.replaceFirst(PREFIX.toLowerCase(Locale.ROOT) + "@", "");
        return EmpireManager.getInstance().getEmpireFromName(arg) != null;
    }

    public EmpireTag(Empire empire) {
        this.empire = empire;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    /*@Override
    public String getObjectType() {
        return PREFIX;
    }*/

    @Override
    public String identify() {
        return PREFIX.toLowerCase(Locale.ROOT) + "@" + empire.getName();
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

    public Empire getEmpire() {
        return empire;
    }

    public boolean equals(EmpireTag residentTag) {
        return residentTag.getEmpire().getId().equals(getEmpire().getId());
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <Empire.name>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the name of the Empire.
        // -->
        tagProcessor.registerTag(ElementTag.class, "name",
                (attribute, object) -> new ElementTag(object.getEmpire().getName()));

        // <--[tag]
        // @attribute <Empire.leader>
        // @returns ResidentTag
        // @plugin Settlements
        // @description
        // Returns the leader of this Empire.
        // -->
        tagProcessor.registerTag(ResidentTag.class, "leader",
                (attribute, object) -> new ResidentTag(object.getEmpire().getCapital().getChief()));


        // <--[tag]
        // @attribute <Empire.capital>
        // @returns SettlementTag
        // @plugin Settlements
        // @description
        // Returns the capital of this Empire.
        // -->
        tagProcessor.registerTag(SettlementTag.class, "leader",
                (attribute, object) -> new SettlementTag(object.getEmpire().getCapital()));

        // <--[tag]
        // @attribute <Empire.online_residents>
        // @returns ListTag(PlayerTag)
        // @plugin Settlements
        // @description
        // Returns the online residents of the Empire.
        // -->
        tagProcessor.registerTag(ListTag.class, "online_residents", (attribute, object) -> {
            List<PlayerTag> members = new ArrayList<>();
            for (Settlement member : object.getEmpire().getMembers()) {
                for (UUID residentId : member.getResidentIds()) {
                    Player player = Bukkit.getPlayer(residentId);
                    if (player != null) {
                        members.add(new PlayerTag(player));
                    }
                }
            }
            return new ListTag(members);
        });

        // <--[tag]
        // @attribute <Empire.residents>
        // @returns ListTag(PlayerTag)
        // @plugin Settlements
        // @description
        // Returns the residents of the Empire.
        // -->
        tagProcessor.registerTag(ListTag.class, "residents", (attribute, object) -> {
            List<PlayerTag> members = new ArrayList<>();
            for (Settlement member : object.getEmpire().getMembers()) {
                for (UUID residentId : member.getResidentIds()) {
                    members.add(new PlayerTag(residentId));
                }
            }
            return new ListTag(members);
        });

        // <--[tag]
        // @attribute <Empire.members>
        // @returns ListTag(SettlementTag)
        // @plugin Settlements
        // @description
        // Returns the member settlements of the Empire.
        // -->
        tagProcessor.registerTag(ListTag.class, "members", (attribute, object) -> {
            List<SettlementTag> members = new ArrayList<>();
            for (Settlement member : object.getEmpire().getMembers()) {
                members.add(new SettlementTag(member));
            }
            return new ListTag(members);
        });

        // <--[tag]
        // @attribute <Empire.level>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the level of the Empire.
        // -->
        tagProcessor.registerTag(ElementTag.class, "level",
                (attribute, object) -> new ElementTag(object.getEmpire().getLevel()));

        // <--[tag]
        // @attribute <Empire.balance>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the balance of the Empire.
        // -->
        tagProcessor.registerTag(ElementTag.class, "balance",
                (attribute, object) -> new ElementTag(object.getEmpire().getBank()));

        // <--[tag]
        // @attribute <Empire.tax>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the tax of the Empire.
        // -->
        tagProcessor.registerTag(ElementTag.class, "tax",
                (attribute, object) -> new ElementTag(object.getEmpire().getTax()));

        // <--[tag]
        // @attribute <Settlement.world>
        // @returns WorldTag
        // @plugin Settlements
        // @description
        // Returns the world of the Empire.
        // -->
        tagProcessor.registerTag(WorldTag.class, "world",
                (attribute, object) -> new WorldTag(object.getEmpire().getWorldName()));

        // <--[tag]
        // @attribute <Empire.home>
        // @returns LocationTag
        // @plugin Settlements
        // @description
        // Returns the home of this empire.
        // -->
        tagProcessor.registerTag(LocationTag.class, "home", (attribute, object) -> {
            if (object.getEmpire().getHome() == null) {
                return null;
            }

            return new LocationTag(object.getEmpire().getHome().toBukkitLocation());
        });

        // <--[tag]
        // @attribute <Empire.upkeep>
        // @returns ElementTag
        // @plugin Settlements
        // @description
        // Returns the upkeep of the empire.
        // -->
        tagProcessor.registerTag(ElementTag.class, "upkeep",
                (attribute, object) -> new ElementTag(object.getEmpire().getUpkeep()));

        for (PlotMode mode : PlotMode.values()) {
            // <--[tag]
            // @attribute <Empire.mode_name>
            // @returns ElementTag
            // @plugin Settlements
            // @description
            // Returns whether the mode is enabled for the empire.
            // -->
            tagProcessor.registerTag(ElementTag.class, "mode_" + mode.name().toLowerCase(Locale.ROOT),
                    (attribute, object) -> new ElementTag(object.getEmpire().getMode(mode)));
        }

        for (Use use : Use.values()) {
            // <--[tag]
            // @attribute <Empire.use_name>
            // @returns ElementTag
            // @plugin Settlements
            // @description
            // Returns the use flag state of the empire.
            // -->
            tagProcessor.registerTag(ElementTag.class, "use_" + use.name().toLowerCase(Locale.ROOT),
                    (attribute, object) -> new ElementTag(object.getEmpire().getUseState(use).name().toLowerCase(Locale.ROOT)));
        }
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EmpireTag
        // @name balance
        // @input ElementTag(Decimal)
        // @plugin Settlements
        // @description
        // Sets the money balance of an empire.
        // @tags
        // <EmpireTag.balance>
        // -->
        if (mechanism.matches("balance")) {
            empire.setBank(mechanism.getValue().asLong());
            DatabaseManager.getInstance().saveEmpire(empire);
            return;
        }

        // <--[mechanism]
        // @object EmpireTag
        // @name balance
        // @input ElementTag(String)
        // @plugin Settlements
        // @description
        // Add a member to this empire
        // @tags
        // <EmpireTag.add_member>
        // -->
        if (mechanism.matches("add_member")) {
            String name = mechanism.getValue().asString();
            Settlement settlement = SettlementManager.getInstance().getFromName(name);
            if (settlement == null) {
                mechanism.echoError("No settlement found for '" + name + "'");
                return;
            }

            settlement.setEmpire(empire);
            if (Config.Empire.Missions.DISTRIBUTE_ON_JOIN) {
                empire.distributeNewMissions(settlement);
            }
            for (Resident member : settlement.getOnlineResidents()) {
                member.addChannel(ChatChannel.EMPIRE);
            }
            InviteCache.getPendingEmpireInvites().remove(settlement.getId(), empire.getId());
            DatabaseManager.getInstance().saveSettlement(settlement);
            DatabaseManager.getInstance().saveEmpire(empire);
            return;
        }

        // <--[mechanism]
        // @object EmpireTag
        // @name balance
        // @input ElementTag(String)
        // @plugin Settlements
        // @description
        // Remove a member from this empire
        // @tags
        // <EmpireTag.remove_member>
        // -->
        if (mechanism.matches("remove_member")) {
            String name = mechanism.getValue().asString();
            Settlement settlement = SettlementManager.getInstance().getFromName(name);
            if (settlement == null || !empire.isMember(settlement)) {
                mechanism.echoError("No member found for '" + name + "'");
                return;
            }

            settlement.leaveEmpire();
            return;
        }
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        Debug.echoError("Cannot apply properties to an empire!");
    }
}
