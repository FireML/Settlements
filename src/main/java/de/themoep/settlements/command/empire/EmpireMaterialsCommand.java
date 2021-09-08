package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.utils.HumanReadable;

import java.util.Map;

public class EmpireMaterialsCommand extends EmpireSubCommand {
    public EmpireMaterialsCommand(SubCommand parent) {
        super(parent, "materials");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Empire empire = resident.getVillage().getEmpire();
        if (empire.isMaxLevel()) {
            getMessage("max-level-reached").tell(resident);
            return true;
        }
        Map<String, Long> nextLevelReqs = empire.getNextLevelRequirements();
        if (nextLevelReqs.isEmpty()) {
            getMessage("max-level-reached").tell(resident);
            return true;
        }

        getMessage("head").tell(resident);
        for (Map.Entry<String, Long> entry : nextLevelReqs.entrySet()) {
            long progress = empire.getAmount(entry.getKey());
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
