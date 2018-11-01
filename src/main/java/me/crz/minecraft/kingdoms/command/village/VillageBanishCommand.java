package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

public class VillageBanishCommand extends VillageSubCommand {

    public VillageBanishCommand(SubCommand parent) {
        super(parent, "banish <name>", VillageRank.CHIEF);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Resident res = ResidentManager.getInstance().getResident(args[0]);
            if (res == null) {
                resident.tell(getError("player-not-found"), "name", args[0]);
                return;
            }
            resident.getVillage().addBanishedResident(res);
            Utils.runSync(() -> {
                Player target = res.getPlayer();
                if (target != null) {
                    Village village = VillageManager.getInstance().getVillageAt(target.getLocation());
                    if (resident.getVillage().equals(village)) {
                        Utils.teleportToSpawn(target);
                    }
                }
            });
            MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("banished"), "player", res.getName());
            if (res.hasVillage() && res.getVillage().equals(resident.getVillage())) {
                VillageManager.getInstance().leaveVillage(res);
            }
            DatabaseManager.getInstance().saveVillage(resident.getVillage());
        });
        return true;
    }
}
