package me.crz.minecraft.kingdoms.command.village.outpost;

import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Collection;

public class OutpostSethomeCommand extends VillageSubCommand {
    public OutpostSethomeCommand(VillageSubCommand parent) {
        super(parent, "sethome");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        OwnedLand currentLand = LandManager.getInstance().getLand(resident.getPlayer().getLocation());
        if (currentLand == null || !currentLand.isOwnedBy(resident.getVillage())) {
            resident.tell(getError("not-an-outpost"));
            return true;
        }
        OwnedLand outpostLand = null;
        if (currentLand.isOutpost()) {
            outpostLand = currentLand;
        } else {
            Collection<OwnedLand> connected = LandManager.getInstance().getAllConnectedLand(currentLand, search -> {
                if (search.isOutpost()) {
                    return LandManager.FilterResponse.RETURN;
                } else if (search.containsHome()) {
                    return LandManager.FilterResponse.SKIP_ENTRY;
                }
                return LandManager.FilterResponse.CONTINUE;
            });
            for (OwnedLand l : connected) {
                if (l.isOutpost()) {
                    outpostLand = l;
                    break;
                }
            }
        }
        if (outpostLand == null) {
            resident.tell(getError("not-an-outpost"));
            return true;
        }
        resident.getVillage().setOutpostHome(outpostLand.getOutpostName(), resident.getPlayer().getLocation());
        if (outpostLand != currentLand) {
            currentLand.setOutpost(outpostLand.getOutpostName());
            outpostLand.setOutpost(null);
        }
        OwnedLand finalOutpostLand = outpostLand;
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveVillage(resident.getVillage());
            DatabaseManager.getInstance().saveLand(currentLand);
            if (finalOutpostLand != currentLand) {
                DatabaseManager.getInstance().saveLand(finalOutpostLand);
            }
        });
        resident.tell(getMessage("success"), "name", currentLand.getOutpostName());
        return true;
    }
}
