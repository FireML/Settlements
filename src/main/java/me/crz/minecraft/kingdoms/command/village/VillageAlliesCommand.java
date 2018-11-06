package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.VillageManager;

import java.util.stream.Collectors;

public class VillageAlliesCommand extends VillageSubCommand {
    public VillageAlliesCommand(SubCommand parent) {
        super(parent, "allies", VillageRank.RESIDENT);
        this.setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        resident.tell(getMessage("head"));
        resident.tell(resident.getVillage().getAllies().stream()
                .map(id -> getMessage("entry").getReplacedText("name", VillageManager.getInstance().getVillageFromId(id).getName()))
                .collect(Collectors.joining(getMessage("separator").getText()))
        );
        return true;
    }
}
