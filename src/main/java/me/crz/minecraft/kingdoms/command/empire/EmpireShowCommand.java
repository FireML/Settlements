package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EmpireShowCommand extends SubCommand {

    public EmpireShowCommand(SubCommand parent) {
        super(parent, "show [<name>]", "info");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length == 0 && sender instanceof Player) {
            Village village = VillageManager.getInstance().getVillageFromPlayer((Player) sender);
            if (village == null || !village.hasEmpire()) {
                getError("not-in-empire").tell(sender);
                return false;
            }
            Utils.runAsync(() -> getMessage("general").tell(sender, village.getEmpire().getReplacements(this)));
        } else if (args.length == 1) {
            String name = String.join(" ", args);
            Utils.runAsync(() -> {
                Empire empire = EmpireManager.getInstance().getEmpireFromName(name);
                Village village = VillageManager.getInstance().getVillageFromName(name);
                Resident target = ResidentManager.getInstance().getResident(name);
                if (empire != null) {
                    if (empire.isAbandoned()) {
                        getMessage("abandoned").tell(sender, empire.getReplacements(this));
                    } else {
                        getMessage("general").tell(sender, empire.getReplacements(this));
                    }
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
