package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageUnclaimCommand extends VillageSubCommand {
    public VillageUnclaimCommand(SubCommand parent) {
        super(parent, "unclaim", VillageRank.ELDER);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Village village = res.getVillage();
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            res.tell(getError("no-village"));
        } else if (!land.getOwnerVillage().equals(village)) {
            res.tell(getError("not-your-village"), "state", land.getOwnerVillage().getState().getTag());
        } else if (land.isOutpost()) {
            res.tell(getMessage("outpost"), "base", base, "outpost", land.getOutpostName());
        } else {
            MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("success"),
                    "player", res.getName(),
                    "world", land.getWorldName(),
                    "x", String.valueOf(land.getX() * 16 + 8),
                    "z", String.valueOf(land.getZ() * 16 + 8));
            LandManager.getInstance().unclaimLand(land);
        }
        return true;
    }
}
