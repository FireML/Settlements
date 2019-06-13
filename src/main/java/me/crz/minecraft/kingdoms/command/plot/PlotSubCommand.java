package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;

public abstract class PlotSubCommand extends ResidentSubCommand {
    private final boolean adminCommand;

    public PlotSubCommand(SubCommand parent, String usage, String... aliases) {
        this(parent, usage, false, aliases);
    }

    public PlotSubCommand(SubCommand parent, String usage, boolean adminCommand, String... aliases) {
        super(parent, usage, aliases);
        this.adminCommand = adminCommand;
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(resident.getPlayer().getLocation());
        if (land != null) {
            if (!testPlotAdmin(resident, land)) {
                return true;
            }
            return run(resident, land, base, alias, args);
        }
        getError("not-a-claim").tell(resident);
        return true;
    }

    public abstract boolean run(Resident res, OwnedLand land, String base, String alias, String... args);

    @Override
    public boolean testAccess(Resident resident) {
        if (!super.testAccess(resident)) {
            return false;
        }
        if (adminCommand) {
            if (resident.hasVillage() && resident.getVillage().canAdminPlots(resident)) {
                return true;
            } else if (resident.hasEmpire() && resident.getVillage().getEmpire().canAdminPlots(resident)) {
                return true;
            }
            getError("cant-admin-plots").tell(resident);
            return false;
        }
        return true;
    }

    @Override
    public boolean hasAccess(Resident resident) {
        if (!super.hasAccess(resident)) {
            return false;
        }
        if (adminCommand) {
            return (resident.hasVillage() && resident.getVillage().canAdminPlots(resident))
                    || (resident.hasEmpire() && resident.getVillage().getEmpire().canAdminPlots(resident));
        }
        return true;
    }

    private boolean testPlotAdmin(Resident resident, OwnedLand land) {
        if (!testParentPlotAdmin(resident, land)) {
            return false;
        }

        if (land.canAdmin(resident)) {
            return true;
        }
        if (!adminCommand && land.isPlotOwner(resident)) {
            return true;
        }
        if (adminCommand) {
            getError("cant-admin-plots").tell(resident);
        } else {
            if (land.isOwnedBy(resident) || land.isEmbassy()) {
                getError("not-your-plot").tell(resident);
            } else {
                getError("not-your-village").tell(resident, "state", land.getLandOwner().getStateTag());
            }
        }
        return false;
    }

    private boolean isPlotAdmin(Resident resident) {
        if (adminCommand) {
            if (resident != null) {
                if (resident.hasVillage() && resident.getVillage().canAdminPlots(resident)) {
                    return true;
                }
                if (resident.hasEmpire() && resident.getVillage().getEmpire().canAdminPlots(resident)) {
                    return true;
                }
            }
        }
        return !adminCommand;
    }

    private boolean testParentPlotAdmin(Resident resident, OwnedLand land) {
        return !(getParent() instanceof PlotSubCommand) || ((PlotSubCommand) getParent()).testPlotAdmin(resident, land);
    }
}
