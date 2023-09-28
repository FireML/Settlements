package de.themoep.settlements.missions;

import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.events.LandownerLeveledEvent;
import de.themoep.settlements.perk.events.PerkUnlockedEvent;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;


public class MissionListener implements Listener {
    private final Cache<Long, Boolean> playerPlaced;
    private final boolean hasBreakMission;
    private final boolean cacheInEmpireWorldOnly;

    public MissionListener() {
        playerPlaced = Caffeine.newBuilder().expireAfterAccess(Math.max(Config.Settlement.Missions.PLACE_CACHE_TIME, Config.Empire.Missions.PLACE_CACHE_TIME), TimeUnit.MINUTES).build();
        hasBreakMission = SettlementManager.getInstance().getMissions().stream().anyMatch(m -> m.getType() == Mission.Type.BREAK)
                || EmpireManager.getInstance().getMissions().stream().anyMatch(m -> m.getType() == Mission.Type.BREAK);
        cacheInEmpireWorldOnly = SettlementManager.getInstance().getMissions().isEmpty()
                && EmpireManager.getInstance().getMissions().stream().noneMatch(EmpireMission::onlyInEmpireWorld);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (hasBreakMission && (!cacheInEmpireWorldOnly || EmpireManager.getInstance().getEmpireFromWorld(event.getPlayer().getWorld()) != null)) {
            playerPlaced.put(WorldUtils.getBlockLocationHash(event.getBlock()), true);
        }
        handle(event.getPlayer(), event.getBlock().getType().name(), Mission.Type.PLACE);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Mission.Type[] types;
        BlockData data = event.getBlock().getBlockData();
        if (!(data instanceof Ageable) || ((Ageable) data).getAge() == ((Ageable) data).getMaximumAge()) {
            types = new Mission.Type[] {Mission.Type.HARVEST, Mission.Type.BREAK};
        } else if (playerPlaced.getIfPresent(WorldUtils.getBlockLocationHash(event.getBlock())) == null) {
            types = new Mission.Type[] {Mission.Type.BREAK};
        } else {
            return;
        }
        handle(event.getPlayer(), event.getBlock().getType().name(), types);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityKill(EntityDeathEvent event) {
        Player killer = null;
        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager();
            if (damager instanceof Player) {
                killer = (Player) damager;
            } else if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player) {
                killer = ((Player) ((Projectile) damager).getShooter());
            }
        }
        if (killer != null) {
            handle(killer, event.getEntity().getType().name(), Mission.Type.KILL);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHarvest(PlayerHarvestBlockEvent event) {
        for (ItemStack stack : event.getItemsHarvested()) {
            handle(event.getPlayer(), stack.getType().name(), Mission.Type.HARVEST);
        }
    }

    private void handle(Player player, String type, Mission.Type... missionTypes) {
        Resident resident = ResidentManager.getInstance().getResident(player);
        if (resident.hasSettlement()) {
            handle(resident, resident.getSettlement(), resident, type, missionTypes);
            if (resident.hasEmpire()) {
                handle(resident, resident.getSettlement().getEmpire(), resident.getSettlement(), type, missionTypes);
            }
        }
    }

    private <M extends Mission> void handle(Resident resident, MissionProvider<M> provider, MissionTaker<M> taker, String type, Mission.Type[] missionTypes) {
        boolean save = false;
        for (MissionData missionData : taker.getMissionData()) {
            if (missionData.getMission().isValid(resident, provider) && missionData.isFinished()) {
                continue;
            }
            for (Mission.Type missionType : missionTypes) {
                if (missionData.getMission().getType() == missionType
                        && missionData.requires(type)) {
                    missionData.addProgress(type, 1);
                    save = true;
                    if (missionData.isFinished()) {
                        missionData.getMission().finish(resident);
                    }
                    break;
                }
            }
        }
        if (save) {
            DatabaseManager.getInstance().saveMissionData(taker);
        }
    }

    @EventHandler
    public void onPerkUnlock(PerkUnlockedEvent event) {
        if (event.getPerk().getUnlockables().contains("missions") && event.getLandOwner() instanceof MissionProvider) {
            ((MissionProvider<?>) event.getLandOwner()).distributeNewMissions();
        }
    }

    @EventHandler
    public void onLevelUp(LandownerLeveledEvent event) {
        if (event.getLeveled() instanceof MissionProvider
                && ((MissionProvider<?>) event.getLeveled()).distributeMissionsOnLevelUp()
                && event.getLeveled().getLevelInfo() != null
                && event.getLeveled().getLevelInfo().getMissions() > 0) {
            ((MissionProvider<?>) event.getLeveled()).distributeAdditionalMissions();
        }
    }

}
