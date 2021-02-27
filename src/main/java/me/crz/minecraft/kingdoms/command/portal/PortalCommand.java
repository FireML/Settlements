package me.crz.minecraft.kingdoms.command.portal;

import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.command.PlayerSubCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.portal.Portal;
import me.crz.minecraft.kingdoms.portal.gui.PortalLinkGui;
import me.crz.minecraft.kingdoms.portal.gui.PortalUnlinkGui;
import me.crz.minecraft.kingdoms.resident.Resident;
import org.bukkit.entity.Player;

public class PortalCommand extends PlayerSubCommand {

    public PortalCommand() {
        super(null, "portal", "netherportal");
        setHelpCommand(new HelpCommand(this));
        registerCommand(new PortalSubCommand(this, "link") {
            @Override
            public boolean run(Resident resident, Portal portal, String base, String alias, String... args) {
                new PortalLinkGui(portal, resident).show();
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

                new PortalUnlinkGui(portal).show(resident);
                return true;
            }
        });
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        return false;
    }
}
