package me.crz.minecraft.kingdoms.command.village.outpost;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class OutpostSethomeCommand extends VillageSubCommand {
    public OutpostSethomeCommand(VillageSubCommand parent) {
        super(parent, "sethome", VillageRank.ELDER);
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(resident.getPlayer().getLocation());
        if (land == null || !land.isOutpost()) {
            resident.tell(getError("not-an-outpost"));
            return true;
        }
        resident.getVillage().addOutpostHome(land.getOutpostName(), resident.getPlayer().getLocation());
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveVillage(resident.getVillage());
            DatabaseManager.getInstance().saveLand(land);
        });
        resident.tell(getMessage("success"), "name", land.getOutpostName());
        return true;
    }
}
