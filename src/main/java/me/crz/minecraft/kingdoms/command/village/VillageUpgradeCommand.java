package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.vault.Levels;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.command.SubCommand;

import java.util.stream.Collectors;

public class VillageUpgradeCommand extends VillageSubCommand {
    public VillageUpgradeCommand(SubCommand parent) {
        super(parent, "upgrade", VillageRank.RESIDENT);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Village v = res.getVillage();
        if (!VillageManager.getInstance().checkLevelUp(v, res)) {
            res.tell(getMessage("not-possible"), "reason", v.isMaxLevel() ? getText("max-level-reached") :
                getText("missing-items") + "\n"
                        + Levels.getLevelMapFromInt(v.getLevel() + 1, v.getState()).entrySet().stream()
                    .filter(e -> !v.getVault().containsKey(e.getKey()) || v.getVault().get(e.getKey()) < e.getValue())
                    .map(e -> getMessage("entry").getReplacedText(
                            "material", e.getKey(),
                            "amount", String.valueOf(e.getValue() - v.getVault().getOrDefault(e.getKey(), 0))))
                    .collect(Collectors.joining("\n"))
            );
        }
        return true;
    }
}
