package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.EmpireSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireDescriptionCommand extends EmpireSubCommand {
    public EmpireDescriptionCommand(SubCommand parent) {
        super(parent, "setdescription <text>", EmpireRank.SENATOR, "description", "setboard", "board");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        res.getVillage().getEmpire().setDescription(String.join(" ", args));
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(res.getVillage()));
        getMessage("success").tell(res,
                "state", res.getVillage().getState().getTag(),
                "description", res.getVillage().getDescription()
        );
        return true;
    }
}
