package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.UtilChat;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.VillageState;
import net.milkbowl.vault.economy.EconomyResponse;

import java.util.regex.Pattern;

public class VillageCreateCommand extends ResidentSubCommand {
    private Pattern allowed = Pattern.compile("[\\w-]+");

    public VillageCreateCommand(SubCommand parent) {
        super(parent, "create <name>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        String name = UtilChat.getInstance().filterCurses(String.join(" ", args).trim());
        if (name.toLowerCase().contains("claims")) {
            getError("invalid-characters").tell(resident, "input", name);
        } else if (villageExists(name)) {
            getError("village-exists").tell(resident, "name", name);
        } else if (name.length() > 20) {
            getError("too-long").tell(resident, "input", name, "amount", String.valueOf(20));
        } else if (!allowed.matcher(name).matches()) {
            getError("invalid-characters").tell(resident, "input", name);
        } else {
            if (Utils.getVillageCreationCost() != 0) {
                if (Base.getEconomy().getBalance(resident.getPlayer()) < Utils.getVillageCreationCost()) {
                    getError("not-enough-money").tell(resident, "amount", String.valueOf(Utils.getVillageCreationCost()));
                    return true;
                }
                EconomyResponse r = Base.getEconomy().withdrawPlayer(resident.getPlayer(), (double) Utils.getVillageCreationCost());
                if (!r.transactionSuccess()) {
                    MessageUtils.tell(resident, "&cAn error occurred, please tell an admin!");
                    return true;
                }
            }
            Village village = new Village(name, resident.getUUID(), 1, VillageState.VILLAGE);
            VillageManager.getInstance().registerNewVillage(resident, village);

            MessageUtils.broadcast(getMessage("broadcast"),
                    "name", village.getName(),
                    "state", village.getState().getTag(),
                    "player", resident.getName());
            getMessage("success").tell(resident, "name", village.getName());
        }
        return true;
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (resident.hasVillage()) {
            getError("free-required").tell(resident);
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return !resident.hasVillage() && super.hasAccess(resident);
    }


    private boolean villageExists(String name) {
        return VillageManager.getInstance().getVillageFromName(name) != null;
    }
}
