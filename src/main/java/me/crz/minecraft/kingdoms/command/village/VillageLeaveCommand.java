package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.Message;
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
            MessageUtils.tell(player, Message.get("not-in-current"), null, null);
            return;
        }
        if (Utils.playerIsChief(player, village)) {
            MessageUtils.tell(player, Message.get("cant-leave-own"), null, null);
            return;
        }
        MessageUtils.tell(player, Message.get("success-leave"), null, null);
        MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("alert-leave"), "{player}", player.getName());
        VillageManager.getInstance().leaveVillage(ResidentManager.getInstance().getResident(player));
    }
}
