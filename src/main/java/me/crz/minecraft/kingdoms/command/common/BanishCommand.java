package me.crz.minecraft.kingdoms.command.common;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.stream.Collectors;

public interface BanishCommand extends ConfigMessages, TargetParser {

    default boolean listBanished(Resident resident, LandOwner landOwner) {
        Utils.runAsync(() -> {
            resident.tell(getMessage("head"));
            if (!landOwner.getBanishedPlayers().isEmpty()) {
                resident.tell(landOwner.getBanishedPlayers().stream()
                        .map(id -> ResidentManager.getInstance().getResident(id))
                        .filter(Objects::nonNull)
                        .map(r -> getMessage("entry").getReplacedText("name", r.getName(), "title", r.getTitle()))
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
                resident.tell(getError("cant-banish"), "name", target.getName(), "title", ((Resident) target).getTitle());
                return;
            }

            if (!source.addBanished(target)) {
                getError("already-banished").tell(resident, "name", target.getName());
                return;
            }

            MessageUtils.tellResidents(source.getOnlineResidents(), getMessage("banished"), "name", target.getName());
            Utils.runSync(() -> {
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
            });
            DatabaseManager.getInstance().save(source);
        });
        return true;
    }

    default boolean unbanish(Resident resident, LandOwner source, String[] args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Targetable target = getTarget(resident, args);

            if (!source.removeBanished(target)) {
                resident.tell(getError("not-banished"), "name", target.getName());
                return;
            }

            MessageUtils.tellResidents(source.getOnlineResidents(), getMessage("unbanished"), "target", target.getName());
            DatabaseManager.getInstance().save(source);
        });
        return true;
    }
}
