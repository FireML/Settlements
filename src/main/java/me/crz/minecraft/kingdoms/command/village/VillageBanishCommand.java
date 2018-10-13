package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;

import java.util.stream.Collectors;

public class VillageBanishCommand extends VillageSubCommand {
    public VillageBanishCommand(SubCommand parent) {
        super(parent, "banish [list|add|remove] <name>", VillageRank.CHIEF);
        setPermission("kingdoms.user");

        registerCommand(new VillageSubCommand(this, "list") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                Utils.runAsync(() -> {
                    resident.tell(getMessage("head"));
                    resident.tell(resident.getVillage().getBanishedResidents().stream()
                            .map(id -> {
                                Resident res = ResidentManager.getInstance().getResident(id);
                                if (res != null) {
                                    return res.getName();
                                }
                                return id.toString();
                            })
                            .map(name -> getMessage("entry").getReplacedText("name", name))
                            .collect(Collectors.joining(getMessage("separator").getText())));
                });
                return true;
            }
        });
        registerCommand(new VillageSubCommand(this, "add <name>") {
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
                    if (res.getVillage().equals(resident.getVillage())) {
                        VillageManager.getInstance().leaveVillage(res);
                    }
                    resident.getVillage().addBanishedResident(res);
                    DatabaseManager.getInstance().saveVillage(resident.getVillage());
                    MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("banished"), "player", res.getName());
                });
                return true;
            }
        });
        registerCommand(new VillageSubCommand(this, "remove <name>") {
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
                    resident.getVillage().removeBanishedResident(res);
                    DatabaseManager.getInstance().saveVillage(resident.getVillage());
                    MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("unbanished"), "player", res.getName());
                });
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident player, String base, String alias, String... args) {
        return false;
    }
}
