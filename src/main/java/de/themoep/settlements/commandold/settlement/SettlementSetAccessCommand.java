package de.themoep.settlements.commandold.settlement;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.land.gui.AccessGUI;
import de.themoep.settlements.resident.Resident;

public class SettlementSetAccessCommand extends SettlementSubCommand {
    public SettlementSetAccessCommand(SubCommand parent) {
        super(parent, "setaccess <access>",  "set", "access", "setprivacy", "privacy");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            new AccessGUI(resident, "settlement", resident.getSettlement()).open();
            return true;
        }
        try {
            LandAccess access = LandAccess.valueOf(String.join("_", args).toUpperCase());
            resident.getSettlement().setAccess(access);
            DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
            resident.tell(getMessage("success").getText(), "access", access.getTag());
        } catch (IllegalArgumentException e) {
            resident.tell(getMessage("usage.head").getText(),
                    "name", name(),
                    "command", "/" + base + " " + alias,
                    "usage", getUsage(base),
                    "type", resident.getSettlement().getStateTag() + " "
            );
            for (LandAccess access : LandAccess.values()) {
                resident.tell(getMessage("usage.entry").getText(),
                        "usage", "/" + base + " " + alias + " " + access.getTag(),
                        "description", getText("access." + access.getKey())
                );
            }
            if (getMessage("usage.footer").exists()) {
                resident.tell(getMessage("usage.footer").getText(), "name", name(), "command", "/" + base + " " + alias);
            }
        }
        return true;
    }
}
