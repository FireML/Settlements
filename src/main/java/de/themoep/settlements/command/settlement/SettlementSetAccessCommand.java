package de.themoep.settlements.command.settlement;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.gui.AccessGUI;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandAccess;

public class SettlementSetAccessCommand extends SettlementSubCommand {
    public SettlementSetAccessCommand(SubCommand parent) {
        super(parent, "setaccess <access>",  "set", "access");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            new AccessGUI().open(resident.getPlayer());
            return true;
        }
        try {
            LandAccess access = LandAccess.valueOf(String.join("_", args).toUpperCase());
            resident.getSettlement().setAccess(access);
            DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
            resident.tell(getMessage("success"), "access", access.getTag());
        } catch (IllegalArgumentException e) {
            resident.tell(getMessage("usage.head"),
                    "name", name(),
                    "command", "/" + base + " " + alias,
                    "usage", getUsage(base),
                    "type", resident.getSettlement().getStateTag() + " "
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
