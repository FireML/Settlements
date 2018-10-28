package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.vault.Levels;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageState;

import java.util.Map;

public class VillageMaterialsCommand extends VillageSubCommand {
    public VillageMaterialsCommand(SubCommand parent) {
        super(parent, "materials", VillageRank.RESIDENT);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (resident.getVillage().isMaxLevel()) {
            resident.tell(getMessage("max-level-reached"), "state", resident.getVillage().getState().getTag());
            return true;
        }
        Village village = resident.getVillage();
        Map<String, Long> nextLevelReqs = Levels.getLevelRequirements(village.getState(), village.getLevel() + 1);
        if (nextLevelReqs == null || nextLevelReqs.isEmpty()) {
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
        return true;
    }
}
