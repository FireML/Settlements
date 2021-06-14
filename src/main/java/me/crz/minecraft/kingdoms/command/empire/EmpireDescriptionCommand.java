package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;

public class EmpireDescriptionCommand extends EmpireSubCommand {
    public EmpireDescriptionCommand(SubCommand parent) {
        super(parent, "setdescription <text>", "description", "setboard", "board");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        res.getVillage().getEmpire().setDescription(Utils.filterAllCurses(String.join(" ", args)));
        DatabaseManager.getInstance().saveVillage(res.getVillage());
        getMessage("success").tell(res,
                "state", res.getVillage().getState().getTag(),
                "description", res.getVillage().getDescription()
        );
        return true;
    }
}
