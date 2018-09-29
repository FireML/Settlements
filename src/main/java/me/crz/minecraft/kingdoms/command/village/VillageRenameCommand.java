package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

import java.util.ArrayList;
import java.util.List;

public class VillageRenameCommand extends SubCommand {
    String allowed;

    public VillageRenameCommand() {
        super("Renames the current village to something else", "rename");
        allowed = "abcdefghijklmnopqrstuvwxyz0123456789_-";
        this.setPermission("village.user");
        this.setUsage("rename <name>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT);
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF)) {
            MessageUtils.tell(player, MessageUtils.Message.RANK_REQUIRED, "rank", VillageRank.CHIEF.getTag(village.getState()));
            return;
        }
        if (args.length == 0) {
            MessageUtils.tell(player, this.getUsage(base));
            return;
        }
        if (VillageManager.getInstance().getVillageFromName(args[0]) != null) {
            MessageUtils.tell(player, MessageUtils.Message.ERROR_VILLAGE_EXISTS, "name", args[0]);
            return;
        }
        boolean invalid = false;
        String[] split;
        for (int length = (split = args[0].split("")).length, i = 0; i < length; ++i) {
            String s = split[i];
            if (!allowed.contains(s.toLowerCase())) {
                invalid = true;
                break;
            }
        }
        if (invalid) {
            MessageUtils.tell(player, "&cThat name contains invalid charecters", null, null);
            return;
        }
        Utils.runAsync(() -> {
            for (Resident resi : village.getResidents()) {
                resi.setVillageName(args[0]);
            }
            DatabaseManager.getInstance().saveResident(village.getResidents());
            List<Village> villagesToSave = new ArrayList<>();
            for (String ally : village.getAllies()) {
                Village allyVillage = VillageManager.getInstance().getVillageFromName(ally);
                if (allyVillage != null) {
                    allyVillage.removeAlly(village);
                    allyVillage.addAlly(args[0]);
                    villagesToSave.add(allyVillage);
                }
            }
            LandManager.getInstance().changeOwner(village, args[0]);
            village.setName(args[0]);
            village.setEnterMessage(MessageUtils.Message.DEFAULT_ENTER.getReplaced("owner", village.getName()));
            village.setLeaveMessage(MessageUtils.Message.DEFAULT_LEAVE.getReplaced("owner", village.getName()));
            villagesToSave.add(village);
            DatabaseManager.getInstance().saveAllVillages(villagesToSave);
            MessageUtils.tellResidents(village.getOnlineResidents(), MessageUtils.Message.VILLAGE_RENAMED, "name", village.getName());
        });
    }
}
