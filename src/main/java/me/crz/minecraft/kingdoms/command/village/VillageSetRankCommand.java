package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Message;
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
        this.setPermission("kingdoms.user");
        this.setUsage("setrank <player> <rank>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length <= 1) {
            MessageUtils.tellList(player, Arrays.asList(
                    "&8&l-------- &r&c&l/" + base + " " + alias + "&r " + "&8&l--------",
                    "/" + base + " setrank <player> <rank>",
                    "&8[&9*&8] &bElder &7- allows mode,claiming,invite,bank,plot,setrank",
                    "&8[&9*&8] &bBuilder &7- allows resident to build on elder and builder only mode",
                    "&8[&9*&8] &bRecruiter &7- allows resident to invite players",
                    "&8[&9*&8] &bResident &7- default rank for members of this settlement",
                    "&aNote: Officers = Elder and Builder"));
            return;
        }
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, Message.get("not-in-current"), null, null);
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (res.getVillageRank().below(VillageRank.ELDER)) {
            MessageUtils.tell(player, Message.get("rank-required"), "rank", VillageRank.ELDER.getTag(village.getState()));
            return;
        }
        Utils.runAsync(() -> {
            Resident tres = ResidentManager.getInstance().getResident(args[0]);
            if (tres == null) {
                MessageUtils.tell(player, Message.get("player-not-found"), null, null);
                return;
            }
            if (!this.isResident(tres, village)) {
                MessageUtils.tell(player, Message.get("not-in-current"), null, null);
                return;
            }
            VillageRank rank;
            try {
                rank = VillageRank.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                MessageUtils.tell(player, Message.get("invalid-rank"), "{rank}", args[1]);
                return;
            }
            if (res.getVillageRank().ordinal() <= tres.getVillageRank().ordinal()) {
                MessageUtils.tell(player, Message.get("cant_set_that_persons_rank"), null, null);
                return;
            }
            if (rank.equals(VillageRank.CHIEF)) {
                MessageUtils.tell(player, "&c&lPlease use &6&c&l/" + base + " transfer <player>&r&c&l to transfer ownership of the " + village.getState().getTag(), null, null);
                return;
            }
            if (res.getVillageRank().ordinal() <= rank.ordinal()) {
                MessageUtils.tell(player, Message.get("rank-required"), "rank", VillageRank.values()[rank.ordinal() + 1].getTag(village.getState()));
                return;
            }
            tres.setVillageRank(rank);
            DatabaseManager.getInstance().saveResident(tres);
            MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("success-set-rank"),
                    "rank", res.getVillageRank().getTag(village.getState()),
                    "setter", player.getName(),
                    "target", tres.getName());
        });
    }

    private boolean isResident(Resident tres, Village village) {
        return tres.getVillageName().equals(village.getName());
    }
}
