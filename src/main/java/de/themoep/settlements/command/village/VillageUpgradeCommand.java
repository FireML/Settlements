package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.utils.HumanReadable;

import java.util.stream.Collectors;

public class VillageUpgradeCommand extends VillageSubCommand {
    public VillageUpgradeCommand(SubCommand parent) {
        super(parent, "upgrade");
        setVisible(false);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Village v = res.getVillage();
        if (!VillageManager.getInstance().checkLevelUp(v, res)) {
            res.tell(getError("not-possible"), "reason", v.isMaxLevel() ? getText("max-level-reached") :
                getText("missing-items") + "\n"
                        + v.getNextLevelRequirements().entrySet().stream()
                    .filter(e -> v.getAmount(e.getKey()) < e.getValue())
                    .map(e -> getMessage("entry").getReplacedText(
                            "material", HumanReadable.humanizedName(e.getKey()),
                            "required", String.valueOf(e.getValue()),
                            "progress", String.valueOf(v.getAmount(e.getKey())),
                            "amount", String.valueOf(e.getValue() - v.getAmount(e.getKey()))
                    )).collect(Collectors.joining("\n"))
            );
        }
        return true;
    }
}
