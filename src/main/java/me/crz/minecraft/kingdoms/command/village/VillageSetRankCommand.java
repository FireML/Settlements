package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;

import java.util.Arrays;

import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageSetRankCommand extends SubCommand {
    public VillageSetRankCommand() {
        super("Sets the rank of a resident", "setrank");
        this.setPermission("village.user");
        this.setUsage("setrank <player> <rank>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length <= 1) {
            MessageUtils.tellList(player, Arrays.asList("&8&l-------- &r&c&l/" + base + " " + alias + "&r " + "&8&l--------", "/" + base + " setrank <player> <rank>", "&8[&9*&8] &bElder &7- allows mode,claiming,invite,bank,plot,setrank", "&8[&9*&8] &bBuilder &7- allows resident to build on officer only mode", "&8[&9*&8] &bRecruiter &7- allows resident to invite players"));
            return;
        }
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT, null, null);
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER)) {
            MessageUtils.tell(player, MessageUtils.Message.RANK_REQUIRED, "rank", VillageRank.ELDER.getTag(village.getState()));
            return;
        }
        Utils.runAsync(() -> {
            Resident tres = ResidentManager.getInstance().getResident(args[0]);
            if (tres == null) {
                MessageUtils.tell(player, MessageUtils.Message.PLAYER_NOT_FOUND, null, null);
                return;
            }
            if (!this.isResident(tres, village)) {
                MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT, null, null);
                return;
            }
            VillageRank rank;
            try {
                rank = VillageRank.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                MessageUtils.tell(player, MessageUtils.Message.INVALID_RANK, "{rank}", args[1]);
                return;
            }
            if (res.getVillageRank().ordinal() <= tres.getVillageRank().ordinal()) {
                MessageUtils.tell(player, MessageUtils.Message.CANT_SET_THAT_PERSONS_RANK, null, null);
                return;
            }
            if (rank.equals(VillageRank.CHIEF)) {
                MessageUtils.tell(player, "&c&lPlease use &6&c&l/" + base + " transfer <player>&r&c&l to transfer ownership of the " + village.getState().name().toLowerCase(), null, null);
                return;
            }
            if (res.getVillageRank().ordinal() <= rank.ordinal()) {
                MessageUtils.tell(player, MessageUtils.Message.RANK_REQUIRED, "rank", VillageRank.values()[rank.ordinal() + 1].getTag(village.getState()));
                return;
            }
            tres.setVillageRank(rank);
            DatabaseManager.getInstance().saveResident(tres);
            MessageUtils.tellResidents(village.getOnlineResidents(), MessageUtils.Message.SUCCESS_SET_RANK,
                    "rank", res.getVillageRank().getTag(village.getState()),
                    "setter", player.getName(),
                    "target", tres.getName());
        });
    }

    private boolean isResident(Resident tres, Village village) {
        return tres.getVillageName().equals(village.getName());
    }
}
