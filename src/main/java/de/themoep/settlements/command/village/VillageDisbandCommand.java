package de.themoep.settlements.command.village;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Settlement;
import de.themoep.settlements.village.SettlementManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.rank.SettlementRank;
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
        if (res.getSettlement().isCapital()) {
            getMessage("capital-cant-disband").tell(res, "state", res.getSettlement().getStateTag(), "name", res.getSettlement().getName());
        } else if (confirm.getIfPresent(res.getUUID()) == null || args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
            confirm.put(res.getUUID(), true);
            getMessage("confirm").tell(res, "command", base + " " + alias + " confirm");
        } else {
            confirm.invalidate(res.getUUID());
            getMessage("success").tell(res.getSettlement().getOnlineResidents(),
                    "rank", SettlementRank.getChief().getTag(res.getSettlement()),
                    "title", res.getSettlementTitle(),
                    "displaytitle", res.getDisplayTitle(),
                    "state", res.getSettlement().getState().getTag(),
                    "chief", res.getSettlement().getName(),
                    "name", res.getSettlement().getName()
            );
            Settlement settlement = res.getSettlement();
            SettlementManager.getInstance().delete(res.getSettlement());
            Message.get("broadcast.settlement-deletion").broadcast(
                    "name", settlement.getName(),
                    "state", settlement.getState().getTag());
        }
        return true;
    }
}
