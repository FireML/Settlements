package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageDisbandCommand extends SubCommand {
    public VillageDisbandCommand() {
        super("Disbands the current village", "disband");
        this.setPermission("village.user");
        this.setUsage("disband confirm");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT, null, null);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF)) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + "&r&c&l!", null, null);
            return;
        }
        if (args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
            MessageUtils.tell(player, "&c&lPlease do /" + base + " " + alias + " confirm to confirm the &r&e&ldisbanding&r&c&l of your village.", null, null);
            return;
        }
        MessageUtils.tellResidents(village.getOnlineResidents(), MessageUtils.Message.SUCCESS_DELETE, null, null);
        VillageManager.getInstance().deleteVillage(village);
    }
}
