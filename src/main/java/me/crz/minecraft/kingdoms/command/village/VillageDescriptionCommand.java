package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class VillageDescriptionCommand extends VillageSubCommand {
    public VillageDescriptionCommand(SubCommand parent) {
        super(parent, "setdescription <text>", VillageRank.ELDER, "description", "setboard", "board");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        res.getVillage().setDescription(String.join(" ", args));
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(res.getVillage()));
        getMessage("success").tell(res,
                "state", res.getVillage().getState().getTag(),
                "description", res.getVillage().getDescription()
        );
        return true;
    }
}
