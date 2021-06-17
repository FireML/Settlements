package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.utils.HumanReadable;

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
                    .filter(e -> empire.getAmount(e.getKey()) < e.getValue())
                    .map(e -> getMessage("entry").getReplacedText(
                            "material", HumanReadable.humanizedName(e.getKey()),
                            "required", String.valueOf(e.getValue()),
                            "progress", String.valueOf(empire.getAmount(e.getKey())),
                            "amount", String.valueOf(e.getValue() - empire.getAmount(e.getKey()))
                    )).collect(Collectors.joining("\n"))
            );
        }
        return true;
    }
}
