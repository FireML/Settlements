package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageLeaveCommand extends SubCommand {
    public VillageLeaveCommand() {
        super("Leave your current village", "leave");
        this.setPermission("village.user");
        this.setUsage("leave");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        if (Utils.playerIsChief(player, village)) {
            MessageUtils.tell(player, MessageUtils.CANT_LEAVE_OWN_MESSAGE, null, null);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        res.setVillageName("");
        res.setVillageRank(VillageRank.RESIDENT);
        ResidentManager.getInstance().saveResident(res);
        MessageUtils.tell(player, MessageUtils.SUCCESS_LEAVE_MESSAGE, null, null);
        MessageUtils.tellResidents(village.getResidents(), MessageUtils.ALERT_LEAVE_MESSAGE, "{player}", player.getName());
    }
}
