package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EmpireShowCommand extends SubCommand {

    public EmpireShowCommand(SubCommand parent) {
        super(parent, "show [<name>]", "info");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length == 0 && sender instanceof Player) {
            Village village = VillageManager.getInstance().getFromPlayer((Player) sender);
            if (village == null || !village.hasEmpire()) {
                getError("not-in-empire").tell(sender);
                return false;
            }
            Utils.runAsync(() -> getMessage("general").tell(sender, village.getEmpire().getReplacements(this)));
        } else if (args.length == 1) {
            String name = String.join(" ", args);
            Utils.runAsync(() -> {
                Empire empire = EmpireManager.getInstance().getEmpireFromName(name);
                Village village = VillageManager.getInstance().getFromName(name);
                Resident target = ResidentManager.getInstance().getResident(name);
                if (empire != null) {
                    getMessage(empire.isAbandoned() ? "abandoned" : "general").tell(sender, empire.getReplacements(this));
                } else if (village != null && village.hasEmpire()) {
                    getMessage("general").tell(sender, village.getEmpire().getReplacements(this));
                }  else if (target != null && target.hasVillage() && target.getVillage().hasEmpire()) {
                    getMessage("general").tell(sender, target.getVillage().getEmpire().getReplacements(this));
                }else{
                    getError("empire-doesnt-exist").tell(sender, "name", name);
                }
            });
        } else {
            return false;
        }
        return true;
    }
}
