package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;

import java.util.Objects;
import java.util.stream.Collectors;

public class EmpireBanishedCommand extends EmpireSubCommand {
    public EmpireBanishedCommand(SubCommand parent) {
        super(parent, "banished", EmpireRank.LEADER, "banishlist", "listbanished");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Utils.runAsync(() -> {
            resident.tell(getMessage("head"));
            if (!resident.getVillage().getEmpire().getBanishedPlayers().isEmpty()) {
                resident.tell(resident.getVillage().getEmpire().getBanishedPlayers().stream()
                        .map(id -> ResidentManager.getInstance().getResident(id))
                        .filter(Objects::nonNull)
                        .map(r -> getMessage("entry").getReplacedText("name", r.getName(), "title", r.getTitle()))
                        .collect(Collectors.joining(getMessage("separator").getText())));
            }
            if (!resident.getVillage().getEmpire().getBanishedVillages().isEmpty()) {
                resident.tell(resident.getVillage().getEmpire().getBanishedVillages().stream()
                        .map(v -> getMessage("entry").getReplacedText("name", v.getName(), "title", v.getStateTag()))
                        .collect(Collectors.joining(getMessage("separator").getText())));
            }
            if (!resident.getVillage().getEmpire().getBanishedEmpires().isEmpty()) {
                resident.tell(resident.getVillage().getEmpire().getBanishedEmpires().stream()
                        .map(e -> getMessage("entry").getReplacedText("name", e.getName(), "title", e.getStateTag()))
                        .collect(Collectors.joining(getMessage("separator").getText())));
            }
        });
        return true;
    }

}
