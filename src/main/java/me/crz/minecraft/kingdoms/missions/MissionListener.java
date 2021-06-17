package me.crz.minecraft.kingdoms.missions;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.EmpireMission;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
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

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        handle(event.getPlayer(), event.getBlock().getType().name(), Mission.Type.PLACE);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Mission.Type[] types;
        BlockData data = event.getBlock().getBlockData();
        if (!(data instanceof Ageable) || ((Ageable) data).getAge() == ((Ageable) data).getMaximumAge()) {
            types = new Mission.Type[] {Mission.Type.HARVEST, Mission.Type.BREAK};
        } else {
            types = new Mission.Type[] {Mission.Type.BREAK};
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
        if (resident.hasEmpire()) {
            boolean save = false;
            for (MissionData missionData : resident.getVillage().getMissionData()) {
                if (missionData.getMission() instanceof EmpireMission
                        && ((EmpireMission) missionData.getMission()).onlyInEmpireWorld()
                        && resident.getVillage().getEmpire().getWorldName() != null
                        && !player.getWorld().getName().equalsIgnoreCase(resident.getVillage().getEmpire().getWorldName())) {
                    continue;
                }
                if (missionData.isFinished()) {
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
                DatabaseManager.getInstance().saveVillage(resident.getVillage());
            }
        }
    }

}
