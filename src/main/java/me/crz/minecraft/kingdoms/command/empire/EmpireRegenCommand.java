package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.concurrent.TimeUnit;

public class EmpireRegenCommand extends EmpireSubCommand {

    public EmpireRegenCommand(SubCommand parent) {
        super(parent, "regen");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Empire empire = res.getVillage().getEmpire();
        if (KingdomsPlugin.getInstance().getConfig().isSet("empires-world.seed")) {
            getError("not-possible").tell(res);
        } else if (empire.isInRegen()) {
            getError("in-progress").tell(res);
        } else if (Utils.getEmpireWorldRegenDuration() > -1 && empire.getCreated() + Utils.getEmpireWorldRegenDuration() * 1000 < System.currentTimeMillis()) {
            getError("no-longer-possible").tell(res, "duration", String.valueOf(TimeUnit.SECONDS.toMinutes(Utils.getEmpireWorldRegenDuration())));
        } else if (EmpireManager.getInstance().regenWorld(empire)) {
            getMessage("success").tell(empire.getOnlineResidents(), "name", empire.getName());
        } else {
            getError("cant-regen").tell(res);
        }
        return true;
    }

    private boolean empireExists(String name) {
        return EmpireManager.getInstance().getEmpireFromName(name) != null;
    }
}
