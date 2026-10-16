package de.themoep.settlements.command.empire;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Message;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EmpireDisbandCommand extends EmpireSubCommand {
    private final Cache<UUID, Boolean> confirm = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(1)).build();

    public EmpireDisbandCommand(SubCommand parent) {
        super(parent, "disband [confirm]");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (confirm.getIfPresent(res.getUUID()) == null || args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
            confirm.put(res.getUUID(), true);
            getMessage("confirm").tell(res, "command", base + " " + alias + " confirm");
        } else {
            confirm.invalidate(res.getUUID());
            getMessage("success").tell(res.getSettlement().getEmpire().getOnlineResidents(),
                    "chief", res.getName(),
                    "chiefrank", SettlementRank.getChief().getTag(res.getSettlement()),
                    "chieftitle", res.getSettlementTitle(),
                    "chiefdisplaytitle", res.getDisplayTitle(),
                    "capital", res.getSettlement().getName(),
                    "capitalstate", res.getSettlement().getState().getTag()
            );
            Empire empire = res.getSettlement().getEmpire();
            EmpireManager.getInstance().deleteEmpire(res.getSettlement().getEmpire());
            Message.get("broadcast.empire-deletion").broadcast("name", empire.getName());
        }
        return true;
    }
}
