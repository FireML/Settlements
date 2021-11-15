package de.themoep.settlements.integration.denizen.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import de.themoep.settlements.integration.denizen.objects.ResidentTag;
import de.themoep.settlements.integration.denizen.objects.SettlementTag;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.events.SettlementJoinEvent;
import de.themoep.settlements.village.events.SettlementLeaveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SettlementScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // settlements settlement [join/leave]
    //
    // @Regex ^on settlements settlement (join|leave)$
    //
    // @Cancellable false
    //
    // @Triggers when someone joins or leaves a settlement
    //
    // @Context
    // <context.settlement> returns the settlement that is joined or left
    // <context.resident> returns the resident that joins or leaves the settlement
    // <context.state> returns the join/leave state ('join' or 'leave').
    //
    // @Plugin Settlements
    //
    // @Switch settlement:<name> to only process the event if the settlement matches the input.
    //
    // @Group Settlements
    //
    // -->

    public Village settlement;
    public Resident resident;
    public String updateType;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("settlements settlement")) {
            return false;
        }
        String type = path.eventArgLowerAt(2);
        if (!type.equals("join") && !type.equals("leave")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(2);
        if (!cmd.equals(updateType)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "settlement", settlement.getName())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "SettlementsSettlementEvent";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("settlement")) {
            return new SettlementTag(settlement);
        }
        if (name.equals("resident")) {
            return new ResidentTag(resident);
        }
        if (name.equals("state")) {
            return new ElementTag(updateType);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onSettlementJoin(SettlementJoinEvent event) {
        this.updateType = "join";
        this.resident = event.getResident();
        this.settlement = event.getSettlement();
        fire(event);
    }

    @EventHandler
    public void onSettlementLeave(SettlementLeaveEvent event) {
        this.updateType = "leave";
        this.resident = event.getResident();
        this.settlement = event.getSettlement();
        fire(event);
    }
}
