package me.crz.minecraft.kingdoms.command.empire;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.resident.Resident;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EmpireLeaveCommand extends EmpireSubCommand {

    private Cache<UUID, Long> confirm;

    public EmpireLeaveCommand(SubCommand parent) {
        super(parent, "leave");
        confirm = Caffeine.newBuilder().expireAfterWrite(Config.COMMAND_CONFIRM_TIME, TimeUnit.SECONDS).build();
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (resident.getVillage().isCapital()) {
            getMessage("cant-leave-own").tell(resident);
        } else if (confirm.getIfPresent(resident.getUUID()) != null) {
            confirm.invalidate(resident.getUUID());
            getMessage("success").tell(resident.getVillage().getOnlineResidents(),
                    "state", resident.getVillage().getState().getTag(),
                    "empire", resident.getVillage().getEmpire().getName()
            );
            getMessage("alert").tell(resident.getVillage().getEmpire().getOnlineResidents(),
                    "name", resident.getVillage().getName(),
                    "state", resident.getVillage().getState().getTag()
            );
            resident.getVillage().leaveEmpire();
        } else {
            confirm.put(resident.getUUID(), System.currentTimeMillis());
            getMessage("confirm").tell(resident, "time", String.valueOf(Config.COMMAND_CONFIRM_TIME));
        }
        return true;
    }
}
