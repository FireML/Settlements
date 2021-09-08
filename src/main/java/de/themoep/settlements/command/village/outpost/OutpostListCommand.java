package de.themoep.settlements.command.village.outpost;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.village.VillageSubCommand;
import de.themoep.settlements.world.NamedLocation;

import java.text.DecimalFormat;

public class OutpostListCommand extends VillageSubCommand {
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#.#");

    public OutpostListCommand(VillageSubCommand parent) {
        super(parent, "list");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        resident.tell(getMessage("head"));
        for (NamedLocation loc : resident.getVillage().getOutpostHomes().values()) {
            resident.tell(getMessage("entry"),
                    "name", loc.getName(),
                    "world", loc.getWorld(),
                    "x", DOUBLE_FORMAT.format(loc.getX()),
                    "y", DOUBLE_FORMAT.format(loc.getY()),
                    "z", DOUBLE_FORMAT.format(loc.getZ())
            );
        }
        return true;
    }
}
