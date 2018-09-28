package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.OfflinePlayer;

import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Comparator;
import java.util.stream.Collectors;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageResListCommand extends SubCommand {
    public VillageResListCommand() {
        super("lists all online residents", "reslist");
        this.setPermission("village.user");
        this.setUsage("reslist");
    }

    @Override
    public void run(Player p, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(p);
        if (village == null) {
            MessageUtils.tell(p, MessageUtils.NOT_IN_CURRENT_MESSAGE);
            return;
        }
        Utils.runAsync(() -> {
            StringBuilder sb = new StringBuilder("&8&l--------&r&c&lResidents&r&8&l--------\n");
            int i = 0;
            for (Resident res : village.getResidents().stream()
                    .sorted(Comparator.comparing(Resident::getVillageRank))
                    .collect(Collectors.toList())) {
                sb.append("&e");
                sb.append(res.getVillageRank().getTag(village.getState()));
                sb.append(" ");
                sb.append(res.getPlayer() != null ? "&a" : "&c");
                sb.append(res.getName());
                if (i + 1 < village.getOnlineResidents().size()) {
                    sb.append("&7, ");
                }
                ++i;
            }
            MessageUtils.tell(p, sb.toString());
        });
    }
}
