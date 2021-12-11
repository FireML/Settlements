package de.themoep.settlements.command.empire;

import de.themoep.settlements.land.gui.AccessGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandAccess;

public class EmpireSetAccessCommand extends EmpireSubCommand {
    public EmpireSetAccessCommand(SubCommand parent) {
        super(parent, "setaccess <access>", "set", "access");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            new AccessGUI("empire", resident.getSettlement().getEmpire()).open(resident.getPlayer());
            return true;
        }
        try {
            LandAccess access = LandAccess.valueOf(String.join("_", args).toUpperCase());
            resident.getSettlement().getEmpire().setAccess(access);
            DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
            resident.tell(getMessage("success"), "access", access.getTag());
        } catch (IllegalArgumentException e) {
            resident.tell(getMessage("usage.head"),
                    "name", name(),
                    "command", "/" + base + " " + alias,
                    "usage", getUsage(base),
                    "type", resident.getSettlement().getEmpire().getStateTag() + " "
            );
            for (LandAccess access : LandAccess.values()) {
                resident.tell(getMessage("usage.entry"),
                        "usage", "/" + base + " " + alias + " " + access.getTag(),
                        "description", getText("access." + access.getKey())
                );
            }
            if (getMessage("usage.footer").exists()) {
                resident.tell(getMessage("usage.footer"), "name", name(), "command", "/" + base + " " + alias);
            }
        }
        return true;
    }
}
