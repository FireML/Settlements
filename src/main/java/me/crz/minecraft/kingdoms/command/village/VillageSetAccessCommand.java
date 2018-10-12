package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageAccess;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

public class VillageSetAccessCommand extends SubCommand {
    public VillageSetAccessCommand() {
        super("Sets the access level for the settlement", "setaccess", "set");
        this.setPermission("kingdoms.user");
        this.setUsage("setaccess <access>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, ChatColor.RED + "/" + base + " " + alias + "<access> - " + Arrays.stream(VillageAccess.values()).map(VillageAccess::humanizedName).collect(Collectors.joining(", ")));
            return;
        }
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, Message.get("not-in-current"));
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (res.getVillageRank().below(VillageRank.CHIEF)) {
            MessageUtils.tell(player, Message.get("rank-required"), "rank", VillageRank.CHIEF.getTag(village.getState()));
            return;
        }
        try {
            VillageAccess access = VillageAccess.valueOf(String.join("_", args).toUpperCase());
            village.setAccess(access);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
        } catch (IllegalArgumentException e) {
            MessageUtils.tellList(player, Arrays.asList(
                    "&8&l-------- &r&c&l/" + base + " " + alias + "&r " + "&8&l--------",
                    "&8[&9*&8] &b/s set open &7- allows everyone to join without an invitation",
                    "&8[&9*&8] &b/s set public &7- everyone can teleport to home but requires invitation to join",
                    "&8[&9*&8] &b/s set ally &7- allows allies to teleport to home",
                    "&8[&9*&8] &b/s set private &7- allows only members to teleport to home"));
        }
    }
}
