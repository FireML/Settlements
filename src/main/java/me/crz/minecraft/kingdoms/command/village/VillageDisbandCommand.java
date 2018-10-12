package me.crz.minecraft.kingdoms.command.village;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VillageDisbandCommand extends SubCommand {
    private Cache<UUID, Boolean> confirm = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    public VillageDisbandCommand() {
        super("Disbands the current village", "disband");
        this.setPermission("kingdoms.user");
        this.setUsage("disband confirm");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, Message.get("not-in-current"), null, null);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        if (res.getVillageRank().below(VillageRank.CHIEF)) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + "&r&c&l!", null, null);
            return;
        }
        if (confirm.getIfPresent(player.getUniqueId()) == null || args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
            confirm.put(player.getUniqueId(), true);
            MessageUtils.tell(player, "&c&lPlease do /" + base + " " + alias + " confirm to confirm the &r&e&ldisbanding&r&c&l of your village.", null, null);
            return;
        }
        MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("success-delete"), null, null);
        VillageManager.getInstance().deleteVillage(village);
    }
}
