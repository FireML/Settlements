package de.themoep.settlements.command.portal;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.portal.Portal;
import de.themoep.settlements.portal.PortalManager;
import de.themoep.settlements.resident.ResidentManager;
import org.bukkit.Material;
import org.bukkit.block.Block;

public abstract class PortalSubCommand extends ResidentSubCommand {
    private final boolean adminCommand;

    public PortalSubCommand(SubCommand parent, String usage, String... aliases) {
        this(parent, usage, false, aliases);
    }

    public PortalSubCommand(SubCommand parent, String usage, boolean adminCommand, String... aliases) {
        super(parent, usage, aliases);
        this.adminCommand = adminCommand;
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Block block = resident.getPlayer().getTargetBlockExact(5);
        if (block != null && block.getType() == Material.NETHER_PORTAL) {
            Portal portal = PortalManager.getInstance().getPortal(block);
            if (portal != null) {
                if (!testPortalAdmin(resident, portal)) {
                    return true;
                }
                return run(resident, portal, base, alias, args);
            }
        }
        getError("no-portal-found").tell(resident);
        return true;
    }

    public abstract boolean run(Resident res, Portal portal, String base, String alias, String... args);

    @Override
    public boolean testAccess(Resident resident) {
        if (!super.testAccess(resident)) {
            return false;
        }
        if (isPortalAdmin(resident)) {
            return true;
        }
        getError("cant-admin-portals").tell(resident);
        return false;
    }

    @Override
    public boolean hasAccess(Resident resident) {
        if (!super.hasAccess(resident)) {
            return false;
        }
        return resident.hasSettlement() && resident.getSettlement().canAdminPortals(resident);
    }

    private boolean testPortalAdmin(Resident resident, Portal portal) {
        if (!testParentPortalAdmin(resident, portal)) {
            return false;
        }

        OwnedLand land = LandManager.getInstance().getLand(portal.getLocation());
        if (land == null) {
            getError("no-settlement").tell(resident);
            return false;
        }

        if (land.isEmbassy() && !land.getLandOwner().isMember(resident)
                && (land.isPlotOwner(resident) || land.getPlotOwnerResident().isFriend(resident))) {
            if (!resident.hasSettlement() || !resident.getSettlement().canAdminPortals(resident)) {
                getError("not-your-settlement").tell(resident, "state", land.getLandOwner().getStateTag());
                return false;
            }
        } else if (!land.getLandOwner().canAdminPortals(resident)) {
            getError("not-your-settlement").tell(resident, "state", land.getLandOwner().getStateTag());
            return false;
        }
        return true;
    }

    private boolean isPortalAdmin(Resident resident) {
        if (adminCommand) {
            if (resident != null) {
                if (resident.hasSettlement() && resident.getSettlement().canAdminPlots(resident)) {
                    return true;
                }
                if (resident.hasEmpire() && resident.getSettlement().getEmpire().canAdminPlots(resident)) {
                    return true;
                }
            }
        }
        return !adminCommand;
    }

    private boolean testParentPortalAdmin(Resident resident, Portal portal) {
        return !(getParent() instanceof PortalSubCommand) || ((PortalSubCommand) getParent()).testPortalAdmin(resident, portal);
    }
}
