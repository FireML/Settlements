package de.themoep.settlements.integration.denizen.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.events.EmpireJoinEvent;
import de.themoep.settlements.empire.events.EmpireLeaveEvent;
import de.themoep.settlements.integration.denizen.objects.EmpireTag;
import de.themoep.settlements.integration.denizen.objects.SettlementTag;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SettlementsEmpireScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // settlements empire [join/leave]
    //
    // @Regex ^on settlements empire (join|leave)$
    //
    // @Cancellable false
    //
    // @Triggers when a settlement joins/leaves an empire
    //
    // @Context
    // <context.empire> returns the empire that is joined or left
    // <context.settlement> returns the settlement that joins or leaves the empire
    // <context.state> returns the join/leave state ('join' or 'leave').
    //
    // @Plugin Settlements
    //
    // @Switch empire:<name> to only process the event if the settlement matches the input.
    //
    // @Group Settlements
    //
    // -->

    public Empire empire;
    public Settlement settlement;
    public String updateType;

    @Override
    public boolean couldMatch(ScriptEvent.ScriptPath path) {
        if (!path.eventLower.startsWith("settlements empire")) {
            return false;
        }
        String type = path.eventArgLowerAt(2);
        if (!type.equals("join") && !type.equals("leave")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptEvent.ScriptPath path) {
        String cmd = path.eventArgLowerAt(2);
        if (!cmd.equals(updateType)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "empire", empire.getName())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("settlement")) {
            return new SettlementTag(settlement);
        }
        if (name.equals("empire")) {
            return new EmpireTag(empire);
        }
        if (name.equals("state")) {
            return new ElementTag(updateType);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEmpireJoin(EmpireJoinEvent event) {
        this.updateType = "join";
        this.settlement = event.getSettlement();
        this.empire = event.getEmpire();
        fire(event);
    }

    @EventHandler
    public void onEmpireLeave(EmpireLeaveEvent event) {
        this.updateType = "leave";
        this.settlement = event.getSettlement();
        this.empire = event.getEmpire();
        fire(event);
    }
}
