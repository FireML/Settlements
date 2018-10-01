package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageLeaveCommand extends SubCommand {
    public VillageLeaveCommand() {
        super("Leave your current village", "leave");
        this.setPermission("kingdoms.user");
        this.setUsage("leave");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT, null, null);
            return;
        }
        if (Utils.playerIsChief(player, village)) {
            MessageUtils.tell(player, MessageUtils.Message.CANT_LEAVE_OWN, null, null);
            return;
        }
        MessageUtils.tell(player, MessageUtils.Message.SUCCESS_LEAVE, null, null);
        MessageUtils.tellResidents(village.getOnlineResidents(), MessageUtils.Message.ALERT_LEAVE, "{player}", player.getName());
        VillageManager.getInstance().leaveVillage(ResidentManager.getInstance().getResident(player));
    }
}
