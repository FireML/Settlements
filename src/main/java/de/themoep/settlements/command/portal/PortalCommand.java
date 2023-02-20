package de.themoep.settlements.command.portal;

import de.themoep.settlements.command.HelpCommand;
import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.portal.Portal;
import de.themoep.settlements.portal.gui.PortalLinkGUI;
import de.themoep.settlements.portal.gui.PortalUnlinkGUI;
import de.themoep.settlements.resident.Resident;
import org.bukkit.entity.Player;

public class PortalCommand extends PlayerSubCommand {

    public PortalCommand() {
        super(null, "portal", "netherportal");
        setHelpCommand(new HelpCommand(this));
        registerCommand(new PortalSubCommand(this, "link") {
            @Override
            public boolean run(Resident resident, Portal portal, String base, String alias, String... args) {
                new PortalLinkGUI(portal, resident).open();
                return true;
            }
        });
        registerCommand(new PortalSubCommand(this, "unlink") {
            @Override
            public boolean run(Resident resident, Portal portal, String base, String alias, String... args) {
                if (portal.getLinks().isEmpty()) {
                    OwnedLand land = LandManager.getInstance().getLand(portal.getLocation());
                    if (land.getLandOwner().canAdminPortals(resident)) {
                        getError("no-portals-linked.create").tell(resident, "state", land.getLandOwner().getStateTag());
                    } else {
                        getError("no-portals-linked.common").tell(resident, "state", land.getLandOwner().getStateTag());
                    }
                    return true;
                }

                new PortalUnlinkGUI(resident, portal).open();
                return true;
            }
        });
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        return false;
    }
}
