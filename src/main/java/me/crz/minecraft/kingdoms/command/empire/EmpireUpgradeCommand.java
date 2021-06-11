package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

import java.util.stream.Collectors;

public class EmpireUpgradeCommand extends EmpireSubCommand {
    public EmpireUpgradeCommand(SubCommand parent) {
        super(parent, "upgrade");
        setVisible(false);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Empire empire = res.getVillage().getEmpire();
        if (!empire.checkLevelUp(res)) {
            res.tell(getError("not-possible"), "reason", empire.isMaxLevel() ? getText("max-level-reached") :
                getText("missing-items") + "\n"
                        + empire.getNextLevelRequirements().entrySet().stream()
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
