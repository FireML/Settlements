package de.themoep.settlements.integration.citizens;

import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.gui.NpcListGUI;
import de.themoep.settlements.npc.NpcConfig;
import de.themoep.settlements.npc.NpcData;
import de.themoep.settlements.npc.NpcManager;
import de.themoep.settlements.npc.NpcType;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.waypoint.WanderWaypointProvider;
import net.citizensnpcs.trait.waypoint.WaypointProvider;
import net.citizensnpcs.trait.waypoint.Waypoints;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Locale;
import java.util.function.Function;

public class NpcCommand extends ResidentSubCommand {

    private final CitizensIntegration citizensIntegration;
    private final Function<Resident, LandOwner<?, ?>> ownerQuery;

    public NpcCommand(CitizensIntegration citizensIntegration, SubCommand parent, Function<Resident, LandOwner<?, ?>> ownerQuery) {
        super(parent, "npc <type> teleport|skin <name>");
        this.citizensIntegration = citizensIntegration;
        this.ownerQuery = ownerQuery;
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        LandOwner<?, ?> landOwner = ownerQuery.apply(resident);
        if (landOwner == null) {
            getError("settlement-required").tell(resident);
            return true;
        }
        if (args.length < 1) {
            new NpcListGUI(resident, landOwner).open();
            return true;
        }
        if (args.length < 2) {
            return false;
        }

        NpcType type = NpcType.parse(args[0]);
        if (type == null) {
            getError("unknown-npc-type").tell(resident, "input", args[0]);
            return false;
        }

        NpcData npcData = landOwner.getNpcData(type);
        if (!npcData.isUnlocked() && npcData.isAvailable()) {
            citizensIntegration.checkForNew(resident, landOwner);
        }
        if (!npcData.isUnlocked() || !npcData.isAvailable()) {
            getError("unknown-npc").tell(resident, "input", args[0], "state", landOwner.getStateTag());
            return false;
        }

        NPC npc = citizensIntegration.getNpc(npcData);

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "tp":
            case "teleport": {
                Location location = resident.getPlayer().getLocation();
                OwnedLand l = LandManager.getInstance().getLand(location);
                if (l != null && l.isOwnedBy(landOwner)) {
                    if (type == NpcType.TRAVELING_MERCHANT) {
                        boolean connectedToHome = LandManager.getInstance().getAllConnectedLand(l, search -> {
                            if (search.containsHome()) {
                                return LandManager.FilterResponse.RETURN;
                            }
                            return LandManager.FilterResponse.CONTINUE;
                        }).size() > 0;
                        if (!connectedToHome) {
                            getError("requires-home").tell(resident, "npc", npcData.getTag());
                            return true;
                        }
                    }
                    citizensIntegration.createNpc(landOwner, type, location);
                    getMessage("teleport.success").tell(resident, "type", type.getTag(), "state", landOwner.getStateTag());
                } else {
                    getError("not-a-claim").tell(resident);
                }
                return true;
            }
            case "skin": {
                if (!resident.hasPermission(getPermission() + ".skin")) {
                    getError("no-permission").tell(resident);
                    return true;
                }

                if (args.length < 3) {
                    return false;
                }

                if (args[2].length() > 16) {
                    getError("player-not-found").tell(resident, "name", args[2]);
                    return true;
                }

                NpcConfig config = NpcManager.getInstance().getConfig(landOwner, type);

                if ("default".equalsIgnoreCase(args[2]) || "reset".equalsIgnoreCase(args[2])) {
                    npcData.setNpcSkinName("");
                } else {
                    npcData.setNpcSkinName(args[2]);
                }
                if (npc.getEntity().getType() == config.getEntityType() && npcData.getNpcSkinName().isEmpty()) {
                    citizensIntegration.applyNpcTraits(npc, config);
                } else {
                    Location location = npc.getStoredLocation();
                    WaypointProvider waypointProvider = npc.getOrAddTrait(Waypoints.class).getCurrentProvider();
                    if (waypointProvider instanceof WanderWaypointProvider) {
                        location = ((WanderWaypointProvider) waypointProvider).getRegionCentres().get(0);
                    }
                    npc.destroy();
                    citizensIntegration.createNpc(landOwner, type, config, location);
                }
                getMessage("skin.success").tell(resident, "type", type.getTag(), "state", landOwner.getStateTag());
                npcData.save();
                return true;
            }
        }
        return false;
    }
}
