package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.EmpireSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.ExecuteMoveUtil;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EmpireHomeCommand extends EmpireSubCommand {
    public EmpireHomeCommand(SubCommand parent) {
        super(parent, "home", EmpireRank.NONE);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Empire empire = resident.getVillage().getEmpire();

        if (empire.getHome() != null) {
            Location loc = empire.getHome().toBukkitLocation();
            getMessage("waiting").tell(resident, "seconds", String.valueOf(3));
            Player player = resident.getPlayer();
            ExecuteMoveUtil.getInstance().execute(player, () -> {
                player.teleport(loc);
                getMessage("success").tell(player, "name", empire.getName());
            }, 3, "Teleport");
        } else {
            getMessage("no-home").tell(resident);
        }
        return true;
    }
}
