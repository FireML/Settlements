package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.db.TownCache;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageAccess;
import me.crz.minecraft.kingdoms.village.VillageManager;

public class VillageJoinCommand extends VillageSubCommand {
    public VillageJoinCommand(SubCommand parent) {
        super(parent, "join <name>");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Village joinVillage = VillageManager.getInstance().getVillageFromName(args[0]);
        if (joinVillage == null) {
            resident.tell(getError("village-doesnt-exist"));
        } else if (joinVillage.getAccess().below(VillageAccess.OPEN) && !Utils.playerHasInvite(resident.getUUID(), joinVillage)) {
            resident.tell(getMessage("no-invite"));
        } else if (joinVillage.isFull()) {
            resident.tell(getMessage("is-full"), "state", joinVillage.getState().getTag(), "name", joinVillage.getName());
        }else if (joinVillage.isBanished(resident.getUUID())) {
            resident.tell(getMessage("banished"), "state", joinVillage.getState().getTag(), "name", joinVillage.getName());
        } else {
            resident.setVillage(joinVillage);
            resident.setVillageRank(VillageRank.RESIDENT);
            resident.addChannel(ChatChannel.SETTLEMENT);
            TownCache.getPendingTownInvites().remove(resident.getUUID(), joinVillage.getId());
            Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(resident));
            resident.tell(getMessage("success"), "name", joinVillage.getName(), "state", joinVillage.getState().getTag());
            MessageUtils.tellResidents(joinVillage.getOnlineResidents(), getMessage("new-resident"), "player", resident.getName());
        }
        return true;
    }
}
