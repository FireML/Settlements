package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.utils.HumanReadable;

import java.util.stream.Collectors;

public class EmpireUpgradeCommand extends EmpireSubCommand {
    public EmpireUpgradeCommand(SubCommand parent) {
        super(parent, "upgrade");
        setVisible(false);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Empire empire = res.getSettlement().getEmpire();
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
