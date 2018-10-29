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

public class EmpireHereCommand extends SubCommand {

    public EmpireHereCommand(SubCommand parent) {
        super(parent, "here");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        Empire empire = EmpireManager.getInstance().getEmpireFromWorld(player.getWorld());
        if (empire == null) {
            getError("not-in-empire").tell(player);
            return true;
        }
        Utils.runAsync(() -> getMessage("general").tell(player, empire.getReplacements(EmpireHereCommand.this)));
        return true;
    }
}
