package me.crz.minecraft.kingdoms.integration;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Permission;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCAccessEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.lock.LockProvider;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Pair;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import javax.rmi.CORBA.Util;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;

public class LWCIntegration extends JavaModule implements LockProvider, ConfigMessages {
    private final LWC lwc;
    private final KingdomsPlugin plugin = KingdomsPlugin.getInstance();

    public LWCIntegration(LWC lwc) {
        this.lwc = lwc;
        lwc.getModuleLoader().registerModule(plugin, this);
        plugin.getLockProviders().add(this);
    }

    @Override
    public void onAccessRequest(LWCAccessEvent event) {
        Resident resident = ResidentManager.getInstance().getResident(event.getPlayer());
        Permission.Access access = event.getAccess();

        Block block = event.getProtection().getBlock();

        if (resident.hasVillage()) {
            OwnedLand land = LandManager.getInstance().getLand(block.getLocation());
            if (land != null) {
                if (land.isOwnedBy(resident.getVillage())) {
                    if (resident.hasPermission("kingdoms.command.village.lock.admin")) {
                        event.setAccess(Permission.Access.ADMIN);
                        return;
                    }
                } else if (resident.hasEmpire() && land.isOwnedBy(resident.getVillage().getEmpire())) {
                    if (resident.hasPermission("kingdoms.command.empire.lock.admin")) {
                        event.setAccess(Permission.Access.ADMIN);
                        return;
                    }
                }
            }
        }

        for (Permission permission : event.getProtection().getPermissions()) {
            if (access == Permission.Access.ADMIN) {
                break;
            }
            if (permission.getAccess().ordinal() <= access.ordinal()) {
                continue;
            }
            switch (permission.getType()) {
                case TOWN:
                    if (resident.hasVillage()) {
                        if (permission.getName().startsWith("empire:") || permission.getName().startsWith("e:")) {
                            Empire empire = EmpireManager.getInstance().getEmpireFromName(permission.getName().split(":")[1]);
                            if (empire != null && empire.isMember(resident.getVillage())) {
                                access = permission.getAccess();
                            }
                        } else {
                            Village village;
                            if ("#this".equalsIgnoreCase(permission.getName())) {
                                village = VillageManager.getInstance().getVillageAt(block.getLocation());
                            } else {
                                village = VillageManager.getInstance().getVillageFromName(permission.getName());
                            }
                            if (resident.getVillage().equals(village)) {
                                access = permission.getAccess();
                            }
                        }
                    }
                    break;
                case REGION:
                    if ("#this".equalsIgnoreCase(permission.getName())) {
                        OwnedLand land = LandManager.getInstance().getLand(block.getLocation());
                        if (land != null) {
                            Pair<Boolean, String> pair = getCanBuild(event.getPlayer(), land, land.getBuildState(), "use");
                            if (pair == null) {
                                pair = getCanBuild(event.getPlayer(), land.getLandOwner(), land.getLandOwner().getBuildState(), "use");
                            }
                            if (pair != null && pair.getLeft()) {
                                access = permission.getAccess();
                            }
                        }
                    }
                    break;
                case GROUP:
                    if (resident.hasVillage()) {
                        String[] split = permission.getName().split(":");
                        Village village;
                        String rankName;

                        if (split.length == 1) {
                            rankName = split[0];
                            village = VillageManager.getInstance().getVillageAt(block.getLocation());
                        } else {
                            rankName = split[1];
                            village = VillageManager.getInstance().getVillageFromName(split[0]);
                        }
                        if (resident.getVillage().equals(village)) {
                            try {
                                VillageRank rank = VillageRank.valueOf(rankName.toUpperCase());
                                if (resident.getVillageRank().atLeast(rank)) {
                                    access = permission.getAccess();
                                }
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                    break;
            }
        }
        event.setAccess(access);

    }

    @Override
    public void onRegisterProtection(LWCProtectionRegisterEvent event) {
        if (!event.getPlayer().hasPermission("kingdoms.bypass.restriction.lwc")) {
            boolean limitedToVillage = plugin.getConfig().getBoolean("only-lock-in-settlement");
            Resident res = ResidentManager.getInstance().getResident(event.getPlayer());
            if (limitedToVillage) {
                OwnedLand land = LandManager.getInstance().getLand(event.getPlayer().getLocation());
                if (land == null) {
                    res.tell(getMessage("only-in-village"));
                    event.setCancelled(true);
                    return;
                }
            }

            Pair<Boolean, String> response = getCanBuildAll(event.getPlayer(), event.getBlock().getLocation());
            if (!response.getLeft()) {
                MessageUtils.tell(event.getPlayer(), response.getRight());
                event.setCancelled(true);
            }
        }
    }

    public LWC getLWC() {
        return lwc;
    }

    @Override
    public boolean isLocked(Block block) {
        return LWC.getInstance().findProtection(block.getLocation()) != null;
    }

    @Override
    public boolean canAccess(Player player, Block block) {
        Protection protection = LWC.getInstance().findProtection(block.getLocation());
        return protection == null || LWC.getInstance().canAccessProtection(player, protection);
    }

    @Override
    public void removeInChunks(Collection<? extends SimpleChunkLocation> chunks) {
        Utils.runAsync(() -> {
            for (SimpleChunkLocation claim : chunks) {
                lwc.fastRemoveProtections(Bukkit.getConsoleSender(),
                        "world = '" + claim.getWorld() + "'"
                                + " AND x >= " + claim.getX() * 16
                                + " AND z >= " + claim.getZ() * 16
                                + " AND x < " + claim.getX() * 16 + 16
                                + " AND z < " + claim.getZ() * 16 + 16
                        , KingdomsPlugin.getInstance().getConfig().getBoolean("remove-locked-blocks"));
            }
        });
    }

    @Override
    public void removeInWorld(World world) {
        Utils.runAsync(() -> lwc.fastRemoveProtections(
                Bukkit.getConsoleSender(),
                "world = '" + world.getName() + "'",
                KingdomsPlugin.getInstance().getConfig().getBoolean("remove-locked-blocks")
        ));
    }

    @Override
    public void renameWorld(String oldName, String newName) {
        Utils.runAsync(() -> {
            PreparedStatement sta = lwc.getPhysicalDatabase().prepare("UPDATE " + lwc.getPhysicalDatabase().getPrefix() + "protections SET world = ? WHERE world = ?;");
            if (sta == null) {
                KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error while trying to rename world of LWC protections");
                return;
            }
            try {
                sta.setString(1, newName);
                sta.setString(2, oldName);
                sta.executeUpdate();
                lwc.getProtectionCache().clear();
            } catch (SQLException e) {
                KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error while trying to rename world of LWC protections", e);
            }
        });
    }

    @Override
    public String name() {
        return "lwc";
    }
}
