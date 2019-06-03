package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.PlayerSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

public class VillageHereCommand extends PlayerSubCommand {

    public VillageHereCommand(SubCommand parent) {
        super(parent, "here");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageAt(player.getLocation());
        if (village == null) {
            getError("no-village").tell(player);
            return true;
        }
        getMessage(village.hasEmpire() ? "empire" : village.isAbandoned() ? "abandoned" : "general")
                .tell(player, village.getReplacements(this));
        return true;
    }
}
