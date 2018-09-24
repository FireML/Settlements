package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAdminRankCommand extends SubCommand
{
    public VillageAdminRankCommand() {
        super("Forces a player to have a rank", new String[] { "rank" });
        this.setPermission("village.admin");
        this.setUsage("join <player> <rank>");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 2) {
            MessageUtils.tell(player, this.getUsage(base));
            return;
        }
        if (Bukkit.getPlayer(args[0]) == null) {
            MessageUtils.tell(player, "&c&lNo &r&6&lplayer&r&c&l with the name &r&6&l{player}&r&c&l is online!", "{player}", args[0]);
            return;
        }
        final String rank = args[1].toUpperCase();
        if (!this.rankIsValid(rank)) {
            MessageUtils.tell(player, MessageUtils.INVALID_RANK_MESSAGE, "{rank}", rank);
            return;
        }
        final Player target = Bukkit.getPlayer(args[0]);
        final Resident tRes = ResidentManager.getInstance().getResident(target);
        final Village village = VillageManager.getInstance().getVillageFromName(tRes.getVillageName());
        if (village == null) {
            MessageUtils.tell(player, "&c&lTarget player isn't in a village!");
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(village.getChief());
        final VillageRank r = VillageRank.valueOf(rank);
        if (r.equals(VillageRank.CHIEF)) {
            res.setVillageRank(tRes.getVillageRank());
            tRes.setVillageRank(r);
            village.setChief(tRes.getUUID());
            VillageManager.getInstance().saveVillage(village);
            ResidentManager.getInstance().saveResident(res);
            ResidentManager.getInstance().saveResident(tRes);
        }
        else {
            tRes.setVillageRank(r);
            ResidentManager.getInstance().saveResident(tRes);
        }
        MessageUtils.tell(player, "&c&lPlayer &6&l " + target.getDisplayName() + "&c&l's rank was set to &6&l" + r.name());
        MessageUtils.tellResidents(village.getResidents(), "&c&lPlayer &6&l " + target.getDisplayName() + "&c&l's rank was set to &6&l" + r.name() + "&c&l by &6&l " + player.getDisplayName());
    }
    
    private boolean rankIsValid(final String arg) {
        VillageRank[] values;
        for (int length = (values = VillageRank.values()).length, i = 0; i < length; ++i) {
            final VillageRank rank = values[i];
            if (rank.name().equals(arg)) {
                return true;
            }
        }
        return false;
    }
}
