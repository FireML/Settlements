package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;

import me.crz.minecraft.kingdoms.db.InviteCache;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAllyCommand extends VillageSubCommand {
    public VillageAllyCommand(SubCommand parent) {
        super(parent, "ally <name>", VillageRank.ELDER);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Village village = res.getVillage();
        Village invitedVillage = VillageManager.getInstance().getVillageFromName(args[0].toLowerCase());
        if (invitedVillage == null) {
            res.tell(getError("village-doesnt-exist"), "name", args[0]);
        } else if (invitedVillage.equals(village)) {
            res.tell(getMessage("cant-ally-yourself"));
        } else if (village.isAlly(invitedVillage)) {
            res.tell(getMessage("already-allied"), "name", invitedVillage.getName());
        } else if (InviteCache.getPendingTownAlly().containsEntry(village.getId(), invitedVillage.getId())) {
            MessageUtils.tellResidents(invitedVillage.getOnlineResidents(), getMessage("now-allied-to"), "name", village.getName(), "state", village.getState().getTag());
            MessageUtils.tellResidents(village.getOnlineResidents(), getMessage("now-allied-to"), "name", invitedVillage.getName(), "state", invitedVillage.getState().getTag());
            InviteCache.getPendingTownAlly().remove(village.getId(), invitedVillage.getId());
            invitedVillage.addAlly(village);
            village.addAlly(invitedVillage);

            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveVillage(invitedVillage);
                DatabaseManager.getInstance().saveVillage(village);
            });
        } else {
            InviteCache.getPendingTownAlly().put(invitedVillage.getId(), village.getId());
            MessageUtils.tellResidents(invitedVillage.getOnlineResidents(), getMessage("invite"), "name", village.getName(), "state", village.getState().getTag());
            MessageUtils.tellResidents(invitedVillage.getOnlineResidents(), getMessage("accept-info"), "name", village.getName(), "state", village.getState().getTag());
            res.tell(getMessage("request-send"), "name", invitedVillage.getName());
        }
        return true;
    }
}
