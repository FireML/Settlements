package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.EmpireSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.vault.Levels;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageState;

import java.util.Map;

public class EmpireMaterialsCommand extends EmpireSubCommand {
    public EmpireMaterialsCommand(SubCommand parent) {
        super(parent, "materials", EmpireRank.NONE);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (resident.getVillage().getEmpire().isMaxLevel()) {
            getMessage("max-level-reached").tell(resident);
            return true;
        }
        Empire empire = resident.getVillage().getEmpire();
        Map<String, Long> nextLevelReqs = Levels.getEmpireLevelRequirements(empire.getLevel() + 1);
        if (nextLevelReqs == null || nextLevelReqs.isEmpty()) {
            getMessage("max-level-reached").tell(resident, "state");
            return true;
        }

        getMessage("head").tell(resident);
        for (Map.Entry<String, Long> entry : nextLevelReqs.entrySet()) {
            getMessage("entry").tell(resident,
                    "material", entry.getKey(),
                    "amount", String.valueOf(entry.getValue() - empire.getVault().getOrDefault(entry.getKey(), (long) 0))
            );
        }
        return true;
    }
}
