package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.land.gui.AccessGUI;
import de.themoep.settlements.resident.Resident;

public class EmpireSetAccessCommand extends EmpireSubCommand {
    public EmpireSetAccessCommand(SubCommand parent) {
        super(parent, "setaccess <access>", "set", "access");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            new AccessGUI(resident, "empire", resident.getSettlement().getEmpire()).open();
            return true;
        }
        try {
            LandAccess access = LandAccess.valueOf(String.join("_", args).toUpperCase());
            resident.getSettlement().getEmpire().setAccess(access);
            DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
            resident.tell(getMessage("success").getComponent(), "access", access.getTag());
        } catch (IllegalArgumentException e) {
            resident.tell(getMessage("usage.head").getComponent(),
                    "name", name(),
                    "command", "/" + base + " " + alias,
                    "usage", getUsage(base),
                    "type", resident.getSettlement().getEmpire().getStateTag() + " "
            );
            for (LandAccess access : LandAccess.values()) {
                resident.tell(getMessage("usage.entry").getComponent(),
                        "usage", "/" + base + " " + alias + " " + access.getTag(),
                        "description", getText("access." + access.getKey())
                );
            }
            if (getMessage("usage.footer").exists()) {
                resident.tell(getMessage("usage.footer").getComponent(), "name", name(), "command", "/" + base + " " + alias);
            }
        }
        return true;
    }
}
