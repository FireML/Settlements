package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.EmpireSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.vault.Levels;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

import java.util.stream.Collectors;

public class EmpireUpgradeCommand extends EmpireSubCommand {
    public EmpireUpgradeCommand(SubCommand parent) {
        super(parent, "upgrade", EmpireRank.NONE);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Empire empire = res.getVillage().getEmpire();
        if (!EmpireManager.getInstance().checkLevelUp(empire, res)) {
            res.tell(getError("not-possible"), "reason", empire.isMaxLevel() ? getText("max-level-reached") :
                getText("missing-items") + "\n"
                        + Levels.getEmpireLevelRequirements(empire.getLevel() + 1).entrySet().stream()
                    .filter(e -> !empire.getVault().containsKey(e.getKey()) || empire.getVault().get(e.getKey()) < e.getValue())
                    .map(e -> getMessage("entry").getReplacedText(
                            "material", e.getKey(),
                            "amount", String.valueOf(e.getValue() - empire.getVault().getOrDefault(e.getKey(), (long) 0))))
                    .collect(Collectors.joining("\n"))
            );
        }
        return true;
    }
}
