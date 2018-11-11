package me.crz.minecraft.kingdoms.command.common;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.db.InviteCache;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.stream.Collectors;

public interface AllyCommand extends LandOwnerCommand {

    default boolean listAllies(Resident resident, LandOwner landOwner) {
        resident.tell(getMessage("head"));
        resident.tell(landOwner.getAllies().stream()
                .map(e -> getMessage("entry").getReplacedText("name", e.getName(), "state", e.getStateTag()))
                .collect(Collectors.joining(getMessage("separator").getText()))
        );
        return true;
    }

    default boolean addAlly(Resident resident, LandOwner source, String[] args) {
        if (args.length < 1) {
            return false;
        }
        LandOwner target = getTarget(resident, args[0]);
        if (target == null) {
            return true;
        }

        if (target.equals(source) || target.equals(resident.getVillage())) {
            resident.tell(getMessage("cant-ally-yourself"));
        } else if (source.isAlly(target)) {
            resident.tell(getMessage("already-allied"), "name", target.getName(), "state", target.getStateTag());
        } else if (InviteCache.hasPendingAlly(source, target)) {
            MessageUtils.tellResidents(target.getOnlineResidents(), getMessage("now-allied-to"), "name", source.getName(), "state", source.getStateTag());
            MessageUtils.tellResidents(source.getOnlineResidents(), getMessage("now-allied-to"), "name", target.getName(), "state", target.getStateTag());
            InviteCache.removePendingAlly(source, target);
            target.addAlly(source);
            source.addAlly(target);

            Utils.runAsync(() -> {
                DatabaseManager.getInstance().save(target);
                DatabaseManager.getInstance().save(source);
            });
        } else {
            InviteCache.addPendingAlly(target, source);
            MessageUtils.tellResidents(target.getOnlineResidents(), getMessage("invite"), "name", source.getName(), "state", source.getStateTag());
            MessageUtils.tellResidents(target.getOnlineResidents(), getMessage("accept-info"), "name", source.getName(), "state", source.getStateTag());
            resident.tell(getMessage("request-send"), "name", target.getName(), "state", target.getStateTag());
        }
        return true;
    }

    default boolean removeAlly(Resident resident, LandOwner source, String[] args) {
        if (args.length < 1) {
            return false;
        }
        LandOwner target = getTarget(resident, args[0]);
        if (target == null) {
            return true;
        }

        if (target.equals(source) || target.equals(resident.getVillage())) {
            resident.tell(getMessage("cant-unally-yourself"));
        } else if (!source.isAlly(target)) {
            resident.tell(getMessage("not-allied"), "name", target.getName(), "state", target.getStateTag());
        } else {
            source.removeAlly(target);
            target.removeAlly(source);
            if (!(source instanceof Village) || ((Village) source).isAllowAllyClaim()) {
                LandManager.getInstance().checkClaimDistance(source, target);
            }

            Utils.runAsync(() -> {
                DatabaseManager.getInstance().save(target);
                DatabaseManager.getInstance().save(source);
            });
            MessageUtils.tellResidents(target.getOnlineResidents(), getMessage("success"), "name", source.getName(), "state", source.getStateTag());
            MessageUtils.tellResidents(source.getOnlineResidents(), getMessage("success"), "name", target.getName(), "state", target.getStateTag());
        }
        return true;
    }
}
