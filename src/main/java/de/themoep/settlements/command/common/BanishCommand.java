package de.themoep.settlements.command.common;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.stream.Collectors;

public interface BanishCommand extends ConfigMessages, TargetParser {

    default boolean listBanished(Resident resident, LandOwner<?, ?> landOwner) {
        Utils.runAsync(() -> {
            resident.tell(getMessage("head"));
            if (!landOwner.getBanishedPlayers().isEmpty()) {
                resident.tell(landOwner.getBanishedPlayers().stream()
                        .map(id -> ResidentManager.getInstance().getResident(id))
                        .filter(Objects::nonNull)
                        .map(r -> getMessage("entry").getReplacedText("name", r.getName(), "title", r.getVillageTitle()))
                        .collect(Collectors.joining(getMessage("separator").getText())));
            }
            if (!landOwner.getBanishedVillageIds().isEmpty()) {
                resident.tell(landOwner.getBanishedVillages().stream()
                        .map(v -> getMessage("entry").getReplacedText("name", v.getName(), "title", v.getStateTag()))
                        .collect(Collectors.joining(getMessage("separator").getText())));
            }
            if (!landOwner.getBanishedEmpireIds().isEmpty()) {
                resident.tell(landOwner.getBanishedEmpires().stream()
                        .map(e -> getMessage("entry").getReplacedText("name", e.getName(), "title", e.getStateTag()))
                        .collect(Collectors.joining(getMessage("separator").getText())));
            }
        });
        return true;
    }

    default boolean banish(Resident resident, LandOwner source, String[] args) {
        if (args.length < 1) {
            return false;
        }
        if (resident.getName().equalsIgnoreCase(args[0])) {
            resident.tell(getError("cant-banish-yourself"));
            return true;
        }
        Utils.runAsync(() -> {
            Targetable target = getTarget(resident, args);
            if (target == null) {
                return;
            }

            if (target.isMember(resident)) {
                getError("cant-banish-yourself").tell(resident);
                return;
            }

            if (source.isChief(target)) {
                getError("cant-banish-leader").tell(resident, "name", target.getName());
                return;
            }

            if (target instanceof Resident
                    && source.isMember((Resident) target)
                    && ((Resident) target).getVillageRank().atLeast(resident.getVillageRank())) {
                resident.tell(getError("cant-banish"), "name", target.getName(), "title", ((Resident) target).getVillageTitle());
                return;
            }
            Utils.runSync(() -> {
                if (!source.addBanished(target)) {
                    getError("already-banished").tell(resident, "name", target.getName());
                    return;
                }

                MessageUtils.tellResidents(source.getOnlineResidents(), getMessage("banished"), "name", target.getName());
                for (Resident r : target.getOnlineResidents()) {
                    Player p = r.getPlayer();
                    if (p != null) {
                        OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                        if (land != null && land.isOwnedBy(source)) {
                            Utils.teleportToSpawn(p);
                        }
                    }
                }
                source.removeMember(target);

                Utils.runAsync(() -> DatabaseManager.getInstance().save(source));
            });
        });
        return true;
    }

    default boolean unbanish(Resident resident, LandOwner source, String[] args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Targetable target = getTarget(resident, args);
            if (target == null) {
                return;
            }

            Utils.runSync(() -> {
                if (!source.removeBanished(target)) {
                    resident.tell(getError("not-banished"), "name", target.getName());
                    return;
                }

                MessageUtils.tellResidents(source.getOnlineResidents(), getMessage("unbanished"), "target", target.getName());
                Utils.runAsync(() -> DatabaseManager.getInstance().save(source));
            });
        });
        return true;
    }
}
