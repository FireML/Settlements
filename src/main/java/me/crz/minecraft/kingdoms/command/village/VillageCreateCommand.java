package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.village.Resident;
import net.milkbowl.vault.economy.EconomyResponse;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.BuildState;
import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.command.SubCommand;

import java.util.regex.Pattern;

public class VillageCreateCommand extends VillageSubCommand {
    private Pattern allowed = Pattern.compile("[\\w-]+");

    public VillageCreateCommand(SubCommand parent) {
        super(parent, "create <name>");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident player, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        String name = args[0];
        if (villageExists(name)) {
            MessageUtils.tell(player, getError("village-exists"), "name", name);
        } else if (name.length() > 20) {
            MessageUtils.tell(player, getError("too-long"), "input", name, "amount", String.valueOf(20));
        } else if (!allowed.matcher(name).matches()) {
            MessageUtils.tell(player, getError("invalid-characters"), "input", name);
        } else {
            if (Utils.getVillageCreationCost() != 0) {
                if (Base.getEconomy().getBalance(player.getPlayer()) < Utils.getVillageCreationCost()) {
                    MessageUtils.tell(player, getError("not-enough-money"), "amount", String.valueOf(Utils.getVillageCreationCost()));
                    return true;
                }
                EconomyResponse r = Base.getEconomy().withdrawPlayer(player.getPlayer(), (double) Utils.getVillageCreationCost());
                if (!r.transactionSuccess()) {
                    MessageUtils.tell(player, "&cAn error occurred, please tell an admin!");
                    return true;
                }
            }
            Village village = new Village(name, player.getUUID(), 1, VillageState.VILLAGE, BuildState.RESIDENTS);
            VillageManager.getInstance().registerNewVillage(village);
            player.setVillage(village);
            player.setVillageRank(VillageRank.CHIEF);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(player));

            MessageUtils.broadcast(getMessage("broadcast"),
                    "name", village.getName(),
                    "state", village.getState().getTag(),
                    "player", player.getName());
            MessageUtils.tell(player, getMessage("success"), "name", village.getName());
        }
        return true;
    }

    private boolean villageExists(String name) {
        return VillageManager.getInstance().getVillageFromName(name) != null;
    }
}
