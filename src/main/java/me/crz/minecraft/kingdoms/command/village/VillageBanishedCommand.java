package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;

import java.util.stream.Collectors;

public class VillageBanishedCommand extends VillageSubCommand {
    public VillageBanishedCommand(SubCommand parent) {
        super(parent, "banished", VillageRank.CHIEF, "banishlist", "listbanished");
        setPermission("kingdoms.user");
    }

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

}
