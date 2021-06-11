package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.Map;

public class VillageMaterialsCommand extends VillageSubCommand {
    public VillageMaterialsCommand(SubCommand parent) {
        super(parent, "materials");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (resident.getVillage().isMaxLevel()) {
            resident.tell(getMessage("max-level-reached"), "state", resident.getVillage().getState().getTag());
            return true;
        }
        Village village = resident.getVillage();
        Map<String, Long> nextLevelReqs = village.getNextLevelRequirements();
        if (nextLevelReqs.isEmpty()) {
            resident.tell(getMessage("max-level-reached"), "state", resident.getVillage().getState().getTag());
            return true;
        }

        resident.tell(getMessage("head"));
        for (Map.Entry<String, Long> entry : nextLevelReqs.entrySet()) {
            resident.tell(getMessage("entry"),
                    "material", entry.getKey(),
                    "amount", String.valueOf(entry.getValue() - village.getVault().getOrDefault(entry.getKey(), (long) 0))
            );
        }
        resident.tell(getMessage("footer"));
        return true;
    }
}
