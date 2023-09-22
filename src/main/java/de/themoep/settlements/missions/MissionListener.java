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
import uk.firedev.poleislib.Utils.BlockUtils;


public class MissionListener implements Listener {

    public MissionListener() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (BlockUtils.isPlayerBroken(event.getBlockPlaced().getLocation(), event.getPlayer().getUniqueId())) {
            return;
        }
        handle(event.getPlayer(), event.getBlock().getType().name(), Mission.Type.PLACE);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (BlockUtils.isPlayerPlaced(event.getBlock())) {
            return;
        }
        Mission.Type[] types;
        BlockData data = event.getBlock().getBlockData();
        if (!(data instanceof Ageable ageable) || ageable.getAge() == ageable.getMaximumAge()) {
            types = new Mission.Type[] {Mission.Type.HARVEST, Mission.Type.BREAK};
        } else {
            types = new Mission.Type[] {Mission.Type.BREAK};
        }
        handle(event.getPlayer(), event.getBlock().getType().name(), types);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityKill(EntityDeathEvent event) {
        Player killer = null;
        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent) {
            Entity damager = damageEvent.getDamager();
            if (damager instanceof Player player) {
                killer = player;
            } else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
                killer = player;
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
