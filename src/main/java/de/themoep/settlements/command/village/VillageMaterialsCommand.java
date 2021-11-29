package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Settlement;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.utils.HumanReadable;

import java.util.Map;

public class VillageMaterialsCommand extends VillageSubCommand {
    public VillageMaterialsCommand(SubCommand parent) {
        super(parent, "materials");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Settlement settlement = resident.getSettlement();
        if (settlement.isMaxLevel()) {
            getMessage("max-level-reached").tell(resident, "state", resident.getSettlement().getState().getTag());
            return true;
        }
        Map<String, Long> nextLevelReqs = settlement.getNextLevelRequirements();
        if (nextLevelReqs.isEmpty()) {
            getMessage("max-level-reached").tell(resident, "state", resident.getSettlement().getState().getTag());
            return true;
        }

        getMessage("head").tell(resident);
        for (Map.Entry<String, Long> entry : nextLevelReqs.entrySet()) {
            long progress = settlement.getAmount(entry.getKey());
            getMessage("requirement." + (progress <= entry.getValue() ? "todo" : "done")).tell(resident,
                    "type", HumanReadable.humanizedName(entry.getKey()),
                    "required", String.valueOf(entry.getValue()),
                    "progress", String.valueOf(progress),
                    "amount", String.valueOf(entry.getValue() - progress)
            );
        }
        getMessage("footer").tell(resident);
        return true;
    }
}
