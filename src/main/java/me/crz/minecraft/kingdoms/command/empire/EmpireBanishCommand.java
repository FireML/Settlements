package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

public class EmpireBanishCommand extends EmpireSubCommand {

    public EmpireBanishCommand(SubCommand parent) {
        super(parent, "banish <player>|[player|village|empire]:<name>", EmpireRank.LEADER);
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        if (resident.getName().equalsIgnoreCase(args[0])
                || resident.getVillage().getName().equalsIgnoreCase(args[0])
                || resident.getVillage().getEmpire().getName().equalsIgnoreCase(args[0])) {
            getError("cant-banish-yourself").tell(resident);
            return true;
        }

        Utils.runAsync(() -> {
            Targetable target;
            if (args[0].startsWith("empire:") || args[0].startsWith("e:")) {
                target = EmpireManager.getInstance().getEmpireFromName(args[0].split(":")[1]);
                if (target == null) {
                    getError("empire-doesnt-exist").tell(resident, "name", args[0].split(":")[1]);
                    return;
                }
            } else if (args[0].startsWith("village:") || args[0].startsWith("v:") || args[0].startsWith("settlement:") || args[0].startsWith("s:")) {
                target = VillageManager.getInstance().getVillageFromName(args[0].split(":")[1]);
                if (target == null) {
                    getError("village-doesnt-exist").tell(resident, "name", args[0].split(":")[1]);
                    return;
                }
            } else {
                String name = args[0];
                if (name.contains(":")) {
                    name = name.split(":")[1];
                }
                target = ResidentManager.getInstance().getResident(name);
                if (target == null) {
                    getError("player-not-found").tell(resident, "name", name);
                    return;
                }
            }

            if (target.isMember(resident)) {
                getError("cant-banish-yourself").tell(resident);
                return;
            }

            if (target.equals(resident.getVillage().getEmpire().getCapital().getChief())) {
                getError("cant-banish-leader").tell(resident);
                return;
            }

            if (target.equals(resident.getVillage().getEmpire().getCapital())) {
                getError("cant-banish-capital").tell(resident);
                return;
            }

            if (!resident.getVillage().getEmpire().addBanished(target)) {
                getError("already-banished").tell(resident, "name", target.getName());
                return;
            }

            MessageUtils.tellResidents(resident.getVillage().getEmpire().getOnlineResidents(), getMessage("banished"), "target", target.getName());
            Utils.runSync(() -> {
                for (Resident r : target.getOnlineResidents()) {
                    Player p = r.getPlayer();
                    if (p != null) {
                        Empire empire = EmpireManager.getInstance().getEmpireFromWorld(p.getWorld());
                        if (resident.getVillage().getEmpire().equals(empire)) {
                            Utils.teleportToSpawn(p);
                        }
                    }
                }
                if (target instanceof Village) {
                    EmpireManager.getInstance().leaveEmpire((Village) target);
                }
            });
            DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire());
        });
        return true;
    }
}
