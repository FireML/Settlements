package de.themoep.settlements.command.village;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.utils.config.Config;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VillageLeaveCommand extends VillageSubCommand {

    private Cache<UUID, Long> confirm;

    public VillageLeaveCommand(SubCommand parent) {
        super(parent, "leave");
        confirm = Caffeine.newBuilder().expireAfterWrite(Config.COMMAND_CONFIRM_TIME, TimeUnit.SECONDS).build();
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (resident.getVillage().isChief(resident)) {
            getMessage("cant-leave-own").tell(resident);
        } else if (confirm.getIfPresent(resident.getUUID()) != null) {
            confirm.invalidate(resident.getUUID());
            getMessage("success").tell(resident);
            getMessage("alert").tell(resident.getVillage().getOnlineResidents(), "player", resident.getName());
            resident.leaveVillage();
        } else {
            confirm.put(resident.getUUID(), System.currentTimeMillis());
            getMessage("confirm").tell(resident, "time", String.valueOf(Config.COMMAND_CONFIRM_TIME));
        }
        return true;
    }
}
