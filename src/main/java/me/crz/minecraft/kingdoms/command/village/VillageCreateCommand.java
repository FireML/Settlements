package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.UtilChat;
import me.crz.minecraft.kingdoms.village.Resident;
import net.milkbowl.vault.economy.EconomyResponse;
import me.crz.minecraft.kingdoms.village.Village;
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
    }

    @Override
    public boolean run(Resident player, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        String name = UtilChat.getInstance().filterCurses(String.join(" ", args).trim());
        if (name.toLowerCase().contains("claims")) {
            getError("invalid-characters").tell(player, "input", name);
        } else if (villageExists(name)) {
            getError("village-exists").tell(player, "name", name);
        } else if (name.length() > 20) {
            getError("too-long").tell(player, "input", name, "amount", String.valueOf(20));
        } else if (!allowed.matcher(name).matches()) {
            getError("invalid-characters").tell(player, "input", name);
        } else {
            if (Utils.getVillageCreationCost() != 0) {
                if (Base.getEconomy().getBalance(player.getPlayer()) < Utils.getVillageCreationCost()) {
                    getError("not-enough-money").tell(player, "amount", String.valueOf(Utils.getVillageCreationCost()));
                    return true;
                }
                EconomyResponse r = Base.getEconomy().withdrawPlayer(player.getPlayer(), (double) Utils.getVillageCreationCost());
                if (!r.transactionSuccess()) {
                    MessageUtils.tell(player, "&cAn error occurred, please tell an admin!");
                    return true;
                }
            }
            Village village = new Village(name, player.getUUID(), 1, VillageState.VILLAGE);
            VillageManager.getInstance().registerNewVillage(player, village);

            MessageUtils.broadcast(getMessage("broadcast"),
                    "name", village.getName(),
                    "state", village.getState().getTag(),
                    "player", player.getName());
            getMessage("success").tell(player, "name", village.getName());
        }
        return true;
    }

    private boolean villageExists(String name) {
        return VillageManager.getInstance().getVillageFromName(name) != null;
    }
}
