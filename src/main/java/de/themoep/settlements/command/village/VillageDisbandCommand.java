package de.themoep.settlements.command.village;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.rank.VillageRank;
import de.themoep.settlements.utils.Message;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VillageDisbandCommand extends VillageSubCommand {
    private Cache<UUID, Boolean> confirm = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    public VillageDisbandCommand(SubCommand parent) {
        super(parent, "disband [confirm]");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (res.getVillage().isCapital()) {
            getMessage("capital-cant-disband").tell(res, "state", res.getVillage().getStateTag(), "name", res.getVillage().getName());
        } else if (confirm.getIfPresent(res.getUUID()) == null || args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
            confirm.put(res.getUUID(), true);
            getMessage("confirm").tell(res, "command", base + " " + alias + " confirm");
        } else {
            confirm.invalidate(res.getUUID());
            getMessage("success").tell(res.getVillage().getOnlineResidents(),
                    "rank", VillageRank.getChief().getTag(res.getVillage()),
                    "title", res.getVillageTitle(),
                    "displaytitle", res.getDisplayTitle(),
                    "state", res.getVillage().getState().getTag(),
                    "chief", res.getVillage().getName(),
                    "name", res.getVillage().getName()
            );
            Village village = res.getVillage();
            VillageManager.getInstance().delete(res.getVillage());
            Message.get("broadcast.village-deletion").broadcast(
                    "name", village.getName(),
                    "state", village.getState().getTag());
        }
        return true;
    }
}
