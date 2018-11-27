package me.crz.minecraft.kingdoms.command.village;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.VillageManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VillageDisbandCommand extends VillageSubCommand {
    private Cache<UUID, Boolean> confirm = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    public VillageDisbandCommand(SubCommand parent) {
        super(parent, "disband [confirm]", VillageRank.CHIEF);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (res.getVillage().isCapital()) {
            getMessage("capital-cant-disband").tell(res, "state", res.getVillage().getStateTag(), "name", res.getVillage().getName());
        } else if (confirm.getIfPresent(res.getUUID()) == null || args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
            confirm.put(res.getUUID(), true);
            getMessage("confirm").tell(res, "command", base + " " + alias + " confirm");
        } else {
            getMessage("success").tell(res.getVillage().getOnlineResidents(),
                    "rank", VillageRank.CHIEF.getTag(res.getVillage()),
                    "title", res.getTitle(),
                    "state", res.getVillage().getState().getTag(),
                    "chief", res.getVillage().getName(),
                    "name", res.getVillage().getName()
            );
            VillageManager.getInstance().deleteVillage(res.getVillage());
        }
        return true;
    }
}
