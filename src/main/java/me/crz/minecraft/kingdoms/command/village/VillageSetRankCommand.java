package me.crz.minecraft.kingdoms.command.village;

import org.bukkit.OfflinePlayer;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import java.util.Arrays;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageSetRankCommand extends SubCommand
{
    public VillageSetRankCommand() {
        super("Sets the rank of a resident", new String[] { "setrank" });
        this.setPermission("village.user");
        this.setUsage("setrank <player> <rank>");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length <= 1) {
            MessageUtils.tellList(player, Arrays.<String>asList("&8&l-------- &r&c&l/" + base + " " + alias + "&r " + "&8&l--------", "/" + base + " setrank <player> <rank>", "&8[&9*&8] &bElder &7- allows mode,claiming,invite,bank,plot,setrank", "&8[&9*&8] &bBuilder &7- allows resident to build on officer only mode", "&8[&9*&8] &bRecruiter &7- allows resident to invite players"));
            return;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER)) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(village.getState()) + " or higher&r&c&l!", null, null);
            return;
        }
        final OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null) {
            MessageUtils.tell(player, MessageUtils.PLAYER_NOT_FOUND_MESSAGE, null, null);
            return;
        }
        final Resident tres = ResidentManager.getInstance().getResident(target.getUniqueId());
        if (!this.isResident(tres, village)) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        final String rank = args[1].toUpperCase();
        if (!this.rankIsValid(rank)) {
            MessageUtils.tell(player, MessageUtils.INVALID_RANK_MESSAGE, "{rank}", rank);
            return;
        }
        if (res.getVillageRank().ordinal() >= tres.getVillageRank().ordinal()) {
            MessageUtils.tell(player, MessageUtils.CANT_SET_THAT_PERSONS_RANK_MESSAGE, null, null);
            return;
        }
        if (VillageRank.valueOf(rank).equals(VillageRank.CHIEF)) {
            MessageUtils.tell(player, "&c&lPlease use &6&c&l/" + base + " transfer <player>&r&c&l to transfer ownership of the " + village.getState().name().toLowerCase(), null, null);
            return;
        }
        if (res.getVillageRank().ordinal() >= VillageRank.valueOf(rank).ordinal()) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + "&r&c&l!", null, null);
            return;
        }
        tres.setVillageRank(VillageRank.valueOf(rank));
        ResidentManager.getInstance().saveResident(tres);
        MessageUtils.tellResidents(village.getResidents(), this.getSuccessSetRankMessage(VillageRank.valueOf(rank).getTag(village.getState()), player, target), null, null);
    }
    
    private String getSuccessSetRankMessage(final String rank, final Player player, final OfflinePlayer target) {
        String message = MessageUtils.SUCCESS_SET_RANK_MESSAGE;
        if (message.contains("{setter}")) {
            message = message.replace("{setter}", player.getName());
        }
        if (message.contains("{target}")) {
            message = message.replace("{target}", target.getName());
        }
        if (message.contains("{rank}")) {
            message = message.replace("{rank}", rank);
        }
        return message;
    }
    
    private boolean rankIsValid(final String arg) {
        VillageRank[] values;
        for (int length = (values = VillageRank.values()).length, i = 0; i < length; ++i) {
            final VillageRank rank = values[i];
            if (rank.name().equals(arg)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isResident(final Resident tres, final Village village) {
        return tres.getVillageName().equals(village.getName());
    }
}
