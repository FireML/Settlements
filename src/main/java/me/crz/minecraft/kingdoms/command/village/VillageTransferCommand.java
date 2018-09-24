package me.crz.minecraft.kingdoms.command.village;

import java.util.UUID;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.utils.OfflinePlayerManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageTransferCommand extends SubCommand
{
    public VillageTransferCommand() {
        super("Transfers leadership of your village", new String[] { "transfer" });
        this.setPermission("village.user");
        this.setUsage("transfer <player>");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base));
            return;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF)) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + " or higher&r&c&l!", null, null);
            return;
        }
        final OfflinePlayerManager manager = OfflinePlayerManager.getInstance();
        final UUID target = manager.uuidFromName(args[0]);
        if (target == null) {
            MessageUtils.tell(player, MessageUtils.PLAYER_NOT_FOUND_MESSAGE, null, null);
            return;
        }
        final Resident tres = ResidentManager.getInstance().getResident(target);
        if (!this.isResident(tres, village)) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        tres.setVillageRank(VillageRank.CHIEF);
        ResidentManager.getInstance().saveResident(tres);
        res.setVillageRank(VillageRank.ELDER);
        ResidentManager.getInstance().saveResident(res);
        village.setChief(tres.getUUID());
        VillageManager.getInstance().saveVillage(village);
        MessageUtils.tellResidents(village.getResidents(), this.getSuccessSetRankMessage(VillageRank.CHIEF.getTag(village.getState()), player, args[0]), null, null);
    }
    
    private String getSuccessSetRankMessage(final String rank, final Player player, final String target) {
        String message = MessageUtils.SUCCESS_SET_RANK_MESSAGE;
        if (message.contains("{setter}")) {
            message = message.replace("{setter}", player.getName());
        }
        if (message.contains("{target}")) {
            message = message.replace("{target}", target);
        }
        if (message.contains("{rank}")) {
            message = message.replace("{rank}", rank);
        }
        return message;
    }
    
    private boolean isResident(final Resident resident, final Village village) {
        return resident.getVillageName().equals(village.getName());
    }
}
