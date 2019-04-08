package me.crz.minecraft.kingdoms.command.village;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VillageLeaveCommand extends VillageSubCommand {

    private Cache<UUID, Long> confirm;

    public VillageLeaveCommand(SubCommand parent) {
        super(parent, "leave");
        confirm = Caffeine.newBuilder().expireAfterWrite(Utils.getCommandConfirmTime(), TimeUnit.SECONDS).build();
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (resident.getVillage().isChief(resident)) {
            getMessage("cant-leave-own").tell(resident);
        } else if (confirm.getIfPresent(resident.getUUID()) != null) {
            getMessage("success").tell(resident);
            getMessage("alert").tell(resident.getVillage().getOnlineResidents(), "player", resident.getName());
            resident.leaveVillage();
        } else {
            confirm.put(resident.getUUID(), System.currentTimeMillis());
            getMessage("confirm").tell(resident, "time", String.valueOf(Utils.getCommandConfirmTime()));
        }
        return true;
    }
}
