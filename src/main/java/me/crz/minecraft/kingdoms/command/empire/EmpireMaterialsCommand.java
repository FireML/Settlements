package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.utils.HumanReadable;
import me.crz.minecraft.kingdoms.village.Village;

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
            long progress = empire.getVault().getOrDefault(entry.getKey(), 0L);
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
