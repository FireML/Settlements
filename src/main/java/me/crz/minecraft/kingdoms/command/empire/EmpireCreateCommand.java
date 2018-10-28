package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.EmpireSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.vault.Levels;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.VillageState;

import java.util.regex.Pattern;

public class EmpireCreateCommand extends EmpireSubCommand {
    private Pattern allowed = Pattern.compile("[\\w-]+");

    public EmpireCreateCommand(SubCommand parent) {
        super(parent, "create <name>", VillageRank.CHIEF);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (resident.getVillage().getState() != VillageState.KINGDOM || !resident.getVillage().isMaxLevel()) {
            getError("max-level-kingdom-required").tell(resident,
                    "state", resident.getVillage().getState().getTag(),
                    "level", String.valueOf(resident.getVillage().getLevel())
            );
            return true;
        }
        if (args.length < 1) {
            return false;
        }
        String name = args[0];
        if (empireExists(name)) {
            getError("empire-exists").tell(resident, "name", name);
        } else if (name.length() > 20) {
            getError("too-long").tell(resident, "input", name, "amount", String.valueOf(20));
        } else if (!allowed.matcher(name).matches()) {
            getError("invalid-characters").tell(resident, "input", name);
        } else {
            if (Utils.getEmpireCreationCost() != 0) {
                if (resident.getVillage().getBank() < Utils.getEmpireCreationCost()) {
                    getError("not-enough-money").tell(resident, "amount", String.valueOf(Utils.getEmpireCreationCost()));
                    return true;
                }
                resident.getVillage().setBank(resident.getVillage().getBank() - Utils.getEmpireCreationCost());
            }
            Empire empire = new Empire(name, resident.getVillage().getId(), 1);
            EmpireManager.getInstance().registerNewEmpire(empire);
            resident.getVillage().setEmpire(empire);
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveVillage(resident.getVillage());
            });

            MessageUtils.broadcast(getMessage("broadcast"),
                    "name", empire.getName(),
                    "capital", resident.getVillage().getName(),
                    "capitalstate", resident.getVillage().getState().getTag(),
                    "player", resident.getName());
            getMessage("success").tell(resident, "name", empire.getName());
        }
        return true;
    }

    private boolean empireExists(String name) {
        return EmpireManager.getInstance().getEmpireFromName(name) != null;
    }
}
