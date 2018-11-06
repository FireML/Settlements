package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.perk.Perkable;
import me.crz.minecraft.kingdoms.perk.Vaultable;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface LandOwner extends Vaultable, Perkable<Perk>, Targetable {
    String getName();

    IdObject getId();

    SimpleLocation getHome();

    String getStateTag();

    List<Resident> getResidents();

    List<Resident> getOnlineResidents();

    boolean canAdminPlots(Resident resident);

    boolean isMember(Resident resident);

    boolean isBanished(UUID playerId);

    @Override
    default void runCommands(Perk perk) {
        Utils.runAsync(() -> {
            List<String> commands = new ArrayList<>();
            for (String command : perk.getCommands()) {
                for (Resident resident : getResidents()) {
                    commands.add(MessageUtils.replacePlaceholders(command,
                            "player", resident.getName(),
                            "playerid", resident.getUUID().toString(),
                            "settlement", getName(),
                            "state", getStateTag(),
                            "level", LandOwner.this instanceof Village ? String.valueOf(((Village) LandOwner.this).getLevel()) : "-1",
                            "perk", perk.getTag()
                    ));
                }
            }
            Utils.runSync(() -> {
                for (String command : commands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            });
        });
    }

    boolean isBuff();

    String getStateKey();

    BuildState getBuildState();

    BuildState getUseState(Use use);

    boolean isChief(Player player);

    boolean isOfficer(Player player);

    boolean isAlly(LandOwner landOwner);

    boolean isTrusted(LandOwner landOwner);
}
