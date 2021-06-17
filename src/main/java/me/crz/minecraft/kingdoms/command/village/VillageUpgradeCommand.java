package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.utils.HumanReadable;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

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
                    .filter(e -> !v.getVault().containsKey(e.getKey()) || v.getVault().get(e.getKey()) < e.getValue())
                    .map(e -> getMessage("entry").getReplacedText(
                            "material", HumanReadable.humanizedName(e.getKey()),
                            "required", String.valueOf(e.getValue()),
                            "progress", String.valueOf(v.getVault().getOrDefault(e.getKey(), 0L)),
                            "amount", String.valueOf(e.getValue() - v.getVault().getOrDefault(e.getKey(), 0L))
                    )).collect(Collectors.joining("\n"))
            );
        }
        return true;
    }
}
