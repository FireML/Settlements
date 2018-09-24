package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAdminJoinCommand extends SubCommand
{
    public VillageAdminJoinCommand() {
        super("Forces a player to join a village", new String[] { "join" });
        this.setPermission("village.admin");
        this.setUsage("join <player> <village>");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 2) {
            MessageUtils.tell(player, this.getUsage(base), null, null);
            return;
        }
        if (Bukkit.getPlayer(args[0]) == null) {
            MessageUtils.tell(player, "&c&lNo &r&6&lplayer&r&c&l with the name &r&6&l{player}&r&c&l is online!", "{player}", args[0]);
            return;
        }
        if (VillageManager.getInstance().getVillageFromName(args[1].toLowerCase()) == null) {
            MessageUtils.tell(player, "&c&lNo &r&6&lvillage&r&c&l with the name &r&6&l{village}&r&c&l exists!", "{village}", args[1]);
            return;
        }
        final Player target = Bukkit.getPlayer(args[0]);
        final Village village = VillageManager.getInstance().getVillageFromName(args[1]);
        final Resident res = ResidentManager.getInstance().getResident(target);
        if (res.getVillageName().equals(village.getName())) {
            MessageUtils.tell(player, "&6&l" + target.getName() + "&r&c&l is already in the &r&6&l" + village.getName(), null, null);
            return;
        }
        res.setVillageName(village.getName());
        res.setVillageRank(VillageRank.RESIDENT);
        ResidentManager.getInstance().saveResident(res);
        MessageUtils.tell(player, "&6&l" + target.getName() + "&r&c&l is now in {townName}", "{townName}", village.getName());
        MessageUtils.tell(target, MessageUtils.SUCCESS_JOIN_MESSAGE, "{townName}", village.getName());
        MessageUtils.tellResidents(village.getResidents(), MessageUtils.NEW_RESIDENT_MESSAGE, "{player}", target.getName());
    }
}
