package me.crz.minecraft.kingdoms.command.village;

import java.util.Iterator;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageRenameCommand extends SubCommand
{
    String allowed;
    
    public VillageRenameCommand() {
        super("Renames the current village to something else", new String[] { "rename" });
        allowed = "abcdefghijklmnopqrstuvwxyz0123456789_-";
        this.setPermission("village.user");
        this.setUsage("rename <name>");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF)) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + "&r&c&l!", null, null);
            return;
        }
        if (args.length == 0) {
            MessageUtils.tell(player, this.getUsage(base));
            return;
        }
        if (VillageManager.getInstance().getVillageFromName(args[0]) != null) {
            MessageUtils.tell(player, "&7&lVillage with the name &6&l" + args[0] + " &7&l already exists!");
            return;
        }
        boolean invalid = false;
        String[] split;
        for (int length = (split = args[0].split("")).length, i = 0; i < length; ++i) {
            final String s = split[i];
            if (!allowed.contains(s.toLowerCase())) {
                invalid = true;
                break;
            }
        }
        if (invalid) {
            MessageUtils.tell(player, "&cThat name contains invalid charecters", null, null);
            return;
        }
        MessageUtils.tellResidents(village.getResidents(), "&7&lVillage has been renamed to &6&l" + args[0], null, null);
        for (final Resident resi : ResidentManager.getInstance().getResidentsForVillage(village)) {
            resi.setVillageName(args[0]);
            ResidentManager.getInstance().saveResident(resi);
        }
        for (final String ally : village.getAllies()) {
            final Village allyVillage = VillageManager.getInstance().getVillageFromName(ally);
            if (allyVillage != null) {
                allyVillage.removeAlly(village);
                allyVillage.addAlly(args[0]);
                VillageManager.getInstance().saveVillage(allyVillage);
            }
        }
        LandManager.getInstance().updateAllLand(args[0], village);
        village.setName(args[0]);
        final String enter = MessageUtils.DEFAULT_ENTER_MESSAGE.replace("{owner}", village.getName());
        final String leave = MessageUtils.DEFAULT_LEAVE_MESSAGE.replace("{owner}", village.getName());
        village.setEnterMessage(enter);
        village.setLeaveMessage(leave);
        VillageManager.getInstance().saveVillage(village, true);
    }
}
