package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

public class EmpireShowCommand extends AbstractEmpireInfoCommand {

    public EmpireShowCommand(SubCommand parent) {
        super(parent, "show [<name>]", "info");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (args.length == 0) {
            Village village = VillageManager.getInstance().getVillageFromPlayer(player);
            if (village == null || !village.hasEmpire()) {
                getError("not-in-empire").tell(player);
                return false;
            }
            Utils.runAsync(() -> getMessage("general").tell(player, getReplacements(village.getEmpire())));
        } else if (args.length == 1) {
            String name = args[0];
            Utils.runAsync(() -> {
                Empire empire = EmpireManager.getInstance().getEmpireFromName(name);
                Village village = VillageManager.getInstance().getVillageFromName(name);
                Resident target = ResidentManager.getInstance().getResident(name);
                if (empire != null) {
                    getMessage("general").tell(player, getReplacements(empire));
                } else if (village != null && village.hasEmpire()) {
                    getMessage("general").tell(player, getReplacements(village.getEmpire()));
                }  else if (target != null && target.hasVillage() && target.getVillage().hasEmpire()) {
                    getMessage("general").tell(player, getReplacements(target.getVillage().getEmpire()));
                }else{
                    getError("empire-doesnt-exist").tell(player, "name", args[0]);
                }
            });
        } else {
            return false;
        }
        return true;
    }
}
