package de.themoep.settlements.command.empire;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.config.Config;

import java.util.concurrent.TimeUnit;

public class EmpireRegenCommand extends EmpireSubCommand {

    public EmpireRegenCommand(SubCommand parent) {
        super(parent, "regen");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Empire empire = res.getSettlement().getEmpire();
        if (!empire.hasWorld()) {
            getError("no-world").tell(res, "state", empire.getStateTag(), "name", empire.getName());
        } else if (SettlementsPlugin.getInstance().getConfig().isSet("empires-world.seed")) {
            getError("not-possible").tell(res);
        } else if (empire.isInRegen()) {
            getError("in-progress").tell(res);
        } else if (Config.Empire.World.REGEN_DURATION > -1 && empire.getCreated() + Config.Empire.World.REGEN_DURATION * 1000 < System.currentTimeMillis()) {
            getError("no-longer-possible").tell(res, "duration", String.valueOf(TimeUnit.SECONDS.toMinutes(Config.Empire.World.REGEN_DURATION)));
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
