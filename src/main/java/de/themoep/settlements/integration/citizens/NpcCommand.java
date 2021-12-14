package de.themoep.settlements.integration.citizens;

import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.npc.NpcData;
import de.themoep.settlements.npc.NpcType;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Locale;
import java.util.function.Function;

public class NpcCommand extends ResidentSubCommand {

    private final CitizensIntegration citizensIntegration;
    private final Function<Resident, LandOwner<?, ?>> ownerQuery;

    public NpcCommand(CitizensIntegration citizensIntegration, SubCommand parent, Function<Resident, LandOwner<?, ?>> ownerQuery) {
        super(parent, "npc <type> teleport");
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
        if (args.length < 2) {
            return false;
        }

        NpcType type = Utils.parseEnum(NpcType.class, args[0]);
        if (type == null) {
            getError("unknown-npc-type").tell(resident, "input", args[0]);
            return false;
        }

        NpcData npcData = landOwner.getNpcData(type);
        if (!npcData.isUnlocked()) {
            getError("unknown-npc").tell(resident, "input", args[0], "state", landOwner.getStateTag());
            return false;
        }

        NPC npc = citizensIntegration.getNpc(landOwner, type);

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "teleport": {
                Location location = resident.getPlayer().getLocation();
                OwnedLand l = LandManager.getInstance().getLand(location);
                if (l != null && l.isOwnedBy(landOwner)) {
                    if (npc != null) {
                        npc.teleport(resident.getPlayer().getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                    } else {
                        citizensIntegration.createNpc(landOwner, type, location);
                    }
                } else {
                    getError("not-a-claim").tell(resident);
                }
                return true;
            }
        }
        return false;
    }
}
