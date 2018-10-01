package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.village.Resident;
import net.milkbowl.vault.economy.EconomyResponse;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.BuildState;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.OfflinePlayer;
import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageCreateCommand extends SubCommand {
    String allowed;

    public VillageCreateCommand() {
        super("Create a new village.", "create");
        allowed = "abcdefghijklmnopqrstuvwxyz0123456789_-";
        this.setPermission("kingdoms.user");
        this.setUsage("create <name>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base), null, null);
            return;
        }
        if (VillageManager.getInstance().getVillageFromPlayer(player) != null) {
            MessageUtils.tell(player, MessageUtils.Message.MUST_LEAVE_CURRENT, null, null);
            return;
        }
        String name = args[0];
        if (this.villageExists(name)) {
            MessageUtils.tell(player, MessageUtils.Message.NAME_TAKEN, null, null);
            return;
        }
        boolean invalid = false;
        String[] split;
        for (int length = (split = name.split("")).length, i = 0; i < length; ++i) {
            String s = split[i];
            if (!allowed.contains(s.toLowerCase())) {
                invalid = true;
                break;
            }
        }
        if (invalid) {
            MessageUtils.tell(player, "&cThat name contains invalid charecters", null, null);
            return;
        }
        if (name.length() > 20) {
            MessageUtils.tell(player, "&cThat name contains invalid charecters", null, null);
            return;
        }
        if (Utils.getVillageCreationCost() != 0) {
            if (Base.getEconomy().getBalance(player) < Utils.getVillageCreationCost()) {
                MessageUtils.tell(player, "&c&lYou don't have the required &r&6&l" + Utils.getVillageCreationCost() + "$&r&c&l to make a village!", null, null);
                return;
            }
            EconomyResponse r = Base.getEconomy().withdrawPlayer(player, (double) Utils.getVillageCreationCost());
            if (!r.transactionSuccess()) {
                MessageUtils.tell(player, "&cAn error occurred, please tell an admin!", null, null);
                return;
            }
        }
        String enter = MessageUtils.Message.DEFAULT_ENTER.getReplaced("owner", name);
        String leave = MessageUtils.Message.DEFAULT_LEAVE.getReplaced("owner", name);
        Village village = new Village(name, player.getUniqueId(), 1, VillageState.VILLAGE, enter, leave, BuildState.RESIDENTS);
        VillageManager.getInstance().registerNewVillage(village);
        Resident res = ResidentManager.getInstance().getResident(player);
        res.setVillage(village);
        res.setVillageRank(VillageRank.CHIEF);
        Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
        MessageUtils.tell(player, MessageUtils.Message.SUCCESS_CREATE, null, null);
    }

    private boolean villageExists(String name) {
        return VillageManager.getInstance().getVillageFromName(name) != null;
    }
}
