package de.themoep.settlements.integration.denizen.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.integration.denizen.objects.EmpireTag;
import de.themoep.settlements.integration.denizen.objects.LandTag;
import de.themoep.settlements.integration.denizen.objects.ResidentTag;
import de.themoep.settlements.integration.denizen.objects.SettlementTag;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.events.LandEnterEvent;
import de.themoep.settlements.land.events.LandLeaveEvent;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SettlementsLandScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // settlements land [enter/exit]
    //
    // @Regex ^on settlements land (enter|exit)$
    //
    // @Cancellable false
    //
    // @Triggers when someone enters or exits owned land
    //
    // @Context
    // <context.landowner> returns the settlement or empire that owns the land
    // <context.resident> returns the resident that joins or leaves the settlement
    // <context.land> returns the join/leave state ('join' or 'leave').
    //
    // @Plugin Settlements
    //
    // @Group Settlements
    //
    // -->

    public LandOwner landOwner;
    public OwnedLand land;
    public Resident resident;
    public String updateType;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("settlements land")) {
            return false;
        }
        String type = path.eventArgLowerAt(2);
        return type.equals("enter") || type.equals("exit");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(2);
        if (!cmd.equals(updateType)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "landowner" -> {
                if (landOwner instanceof Empire e) {
                    yield new EmpireTag(e);
                }
                if (landOwner instanceof Settlement s) {
                    yield new SettlementTag(s);
                }
                yield null;
            }
            case "land" -> new LandTag(land);
            case "resident" -> new ResidentTag(resident);
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onSettlementJoin(LandEnterEvent event) {
        this.updateType = "enter";
        this.resident = ResidentManager.getInstance().getResident(event.getPlayer());
        this.landOwner = event.getTo().getLandOwner();
        fire(event);
    }

    @EventHandler
    public void onSettlementLeave(LandLeaveEvent event) {
        this.updateType = "exit";
        this.resident = ResidentManager.getInstance().getResident(event.getPlayer());
        this.landOwner = event.getFrom().getLandOwner();
        fire(event);
    }
}
