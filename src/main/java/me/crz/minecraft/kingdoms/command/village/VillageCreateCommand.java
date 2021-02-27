package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.resident.Resident;
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
        String name = Utils.filterAllCurses(String.join(" ", args).trim());
        if (name.toLowerCase().contains("claims")) {
            getError("invalid-characters").tell(resident, "input", name);
        } else if (villageExists(name)) {
            getError("village-exists").tell(resident, "name", name);
        } else if (name.length() > 20) {
            getError("too-long").tell(resident, "input", name, "amount", String.valueOf(20));
        } else if (!allowed.matcher(name).matches()) {
            getError("invalid-characters").tell(resident, "input", name);
        } else {
            if (Config.Settlement.CREATION_COST != 0) {
                if (!KingdomsPlugin.getInstance().getEconomy().has(resident.getPlayer(), Config.Settlement.CREATION_COST)) {
                    getError("not-enough-money").tell(resident, "amount", String.valueOf(Config.Settlement.CREATION_COST));
                    return true;
                }
                EconomyResponse r = KingdomsPlugin.getInstance().getEconomy().withdrawPlayer(resident.getPlayer(), (double) Config.Settlement.CREATION_COST);
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
