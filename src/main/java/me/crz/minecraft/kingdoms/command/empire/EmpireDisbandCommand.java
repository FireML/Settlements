package me.crz.minecraft.kingdoms.command.empire;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EmpireDisbandCommand extends EmpireSubCommand {
    private Cache<UUID, Boolean> confirm = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

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
            getMessage("success").tell(res.getVillage().getEmpire().getOnlineResidents(),
                    "chief", res.getName(),
                    "chiefrank", VillageRank.getChief().getTag(res.getVillage()),
                    "chieftitle", res.getVillageTitle(),
                    "chiefdisplaytitle", res.getDisplayTitle(),
                    "capital", res.getVillage().getName(),
                    "capitalstate", res.getVillage().getState().getTag()
            );
            Empire empire = res.getVillage().getEmpire();
            EmpireManager.getInstance().deleteEmpire(res.getVillage().getEmpire());
            Message.get("broadcast.empire-deletion").broadcast("name", empire.getName());
        }
        return true;
    }
}
