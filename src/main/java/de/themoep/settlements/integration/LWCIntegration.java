package de.themoep.settlements.integration;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Permission;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCAccessEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.lock.LockProvider;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Pair;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.world.SimpleChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;

public class LWCIntegration extends JavaModule implements LockProvider, ConfigMessages {
    private final LWC lwc;
    private final SettlementsPlugin plugin = SettlementsPlugin.getInstance();

    public LWCIntegration() {
        this.lwc = LWC.getInstance();
        lwc.getModuleLoader().registerModule(plugin, this);
        plugin.getLockProviders().add(this);
    }

    @Override
    public void onAccessRequest(LWCAccessEvent event) {
        Resident resident = ResidentManager.getInstance().getResident(event.getPlayer());
        Permission.Access access = event.getAccess();

        Block block = event.getProtection().getBlock();

        if (resident.hasSettlement()) {
            OwnedLand land = LandManager.getInstance().getLand(block.getLocation());
            if (land != null) {
                if (land.isOwnedBy(resident.getSettlement())) {
                    if (resident.hasPermission("settlements.command.settlement.lock.admin")) {
                        event.setAccess(Permission.Access.ADMIN);
                        return;
                    }
                } else if (resident.hasEmpire() && land.isOwnedBy(resident.getSettlement().getEmpire())) {
                    if (resident.hasPermission("settlements.command.empire.lock.admin")) {
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
                    if (resident.hasSettlement()) {
                        if (permission.getName().startsWith("empire:") || permission.getName().startsWith("e:")) {
                            Empire empire = EmpireManager.getInstance().getEmpireFromName(permission.getName().split(":")[1]);
                            if (empire != null && empire.isMember(resident.getSettlement())) {
                                access = permission.getAccess();
                            }
                        } else {
                            Settlement settlement;
                            if ("#this".equalsIgnoreCase(permission.getName())) {
                                settlement = SettlementManager.getInstance().getAt(block.getLocation());
                            } else {
                                settlement = SettlementManager.getInstance().getFromName(permission.getName());
                            }
                            if (resident.getSettlement().equals(settlement)) {
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
                    if (resident.hasSettlement()) {
                        String[] split = permission.getName().split(":");
                        Settlement settlement;
                        String rankName;

                        if (split.length == 1) {
                            rankName = split[0];
                            settlement = SettlementManager.getInstance().getAt(block.getLocation());
                        } else {
                            rankName = split[1];
                            settlement = SettlementManager.getInstance().getFromName(split[0]);
                        }
                        if (resident.getSettlement().equals(settlement)) {
                            try {
                                SettlementRank rank = SettlementRank.valueOf(rankName.toUpperCase());
                                if (resident.getSettlementRank().atLeast(rank)) {
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
        if (!event.getPlayer().hasPermission("settlements.bypass.restriction.lwc")) {
            Resident res = ResidentManager.getInstance().getResident(event.getPlayer());
            if (Config.Lock.ONLY_IN_CLAIM) {
                OwnedLand land = LandManager.getInstance().getLand(event.getPlayer().getLocation());
                if (land == null) {
                    res.tell(getMessage("only-in-claim"));
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
                        , Config.Lock.Unclaim.REMOVE_BLOCK);
            }
        });
    }

    @Override
    public void removeInWorld(World world) {
        Utils.runAsync(() -> lwc.fastRemoveProtections(
                Bukkit.getConsoleSender(),
                "world = '" + world.getName() + "'",
                Config.Lock.Unclaim.REMOVE_BLOCK
        ));
    }

    @Override
    public void renameWorld(String oldName, String newName) {
        Utils.runAsync(() -> {
            PreparedStatement sta = lwc.getPhysicalDatabase().prepare("UPDATE " + lwc.getPhysicalDatabase().getPrefix() + "protections SET world = ? WHERE world = ?;");
            if (sta == null) {
                SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error while trying to rename world of LWC protections");
                return;
            }
            try {
                sta.setString(1, newName);
                sta.setString(2, oldName);
                sta.executeUpdate();
                lwc.getProtectionCache().clear();
            } catch (SQLException e) {
                SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error while trying to rename world of LWC protections", e);
            }
        });
    }

    @Override
    public String name() {
        return "lwc";
    }
}
