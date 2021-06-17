package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.utils.HumanReadable;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.Map;

public class VillageMaterialsCommand extends VillageSubCommand {
    public VillageMaterialsCommand(SubCommand parent) {
        super(parent, "materials");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Village village = resident.getVillage();
        if (village.isMaxLevel()) {
            getMessage("max-level-reached").tell(resident, "state", resident.getVillage().getState().getTag());
            return true;
        }
        Map<String, Long> nextLevelReqs = village.getNextLevelRequirements();
        if (nextLevelReqs.isEmpty()) {
            getMessage("max-level-reached").tell(resident, "state", resident.getVillage().getState().getTag());
            return true;
        }

        getMessage("head").tell(resident);
        for (Map.Entry<String, Long> entry : nextLevelReqs.entrySet()) {
            long progress = village.getAmount(entry.getKey());
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
