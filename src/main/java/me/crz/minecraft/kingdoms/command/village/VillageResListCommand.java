package me.crz.minecraft.kingdoms.command.village;

import org.bukkit.OfflinePlayer;
import java.util.Iterator;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.village.Resident;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import me.crz.minecraft.kingdoms.rank.VillageRank0;
import java.util.List;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageResListCommand extends SubCommand
{
    public VillageResListCommand() {
        super("lists all online residents", new String[] { "reslist" });
        this.setPermission("village.user");
        this.setUsage("reslist");
    }
    
    @Override
    public void run(final Player p, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
        if (village == null) {
            MessageUtils.tell(p, MessageUtils.NOT_IN_CURRENT_MESSAGE);
            return;
        }
        final StringBuilder sb = new StringBuilder("&8&l--------&r&c&lResidents&r&8&l--------\n");
        int i = 0;
        for (final Resident res : (List<? super Object>)village.getResidents().stream().sorted((r1, r2) -> VillageRank0.valueOf(r1.getVillageRank().name()).compareTo(VillageRank0.valueOf(r2.getVillageRank().name()))).<List<? super Object>, ?>collect((Collector<? super Object, ?, List<? super Object>>)Collectors.<Object>toList())) {
            final OfflinePlayer player = Bukkit.getOfflinePlayer(res.getUUID());
            sb.append("&e");
            sb.append(res.getVillageRank().getTag(village.getState()));
            sb.append(" ");
            sb.append(player.isOnline() ? "&a" : "&c");
            sb.append(player.getName());
            if (i + 1 < village.getResidents().size()) {
                sb.append("&7, ");
            }
            ++i;
        }
        MessageUtils.tell(p, sb.toString());
    }
    
    private static /* synthetic */ int lambda$0(final Resident r1, final Resident r2) {
        return VillageRank0.valueOf(r1.getVillageRank().name()).compareTo(VillageRank0.valueOf(r2.getVillageRank().name()));
    }
}
