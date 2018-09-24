package me.crz.minecraft.kingdoms.land;

import java.util.ArrayList;
import me.crz.minecraft.kingdoms.village.VillageState;
import java.util.List;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.plugin.Plugin;
import me.crz.minecraft.kingdoms.village.Village;
import java.util.Iterator;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.Bukkit;

public class LandPerks
{
    public LandPerks() {
        super();
    }
    
    public void runTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)KingdomsPlugin.getInstance(), (Runnable)new Runnable() {
            final /* synthetic */ LandPerks this$0;
            
            LandPerks$1() {
                this$0 = this$0;
                super();
            }
            
            @Override
            public void run() {
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    if (ResidentManager.getInstance().doesResidentHaveVillage(player)) {
                        final OwnedLand land = LandManager.getInstance().getLandFromLocation(player.getLocation());
                        if (land == null) {
                            continue;
                        }
                        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
                        if (village == null) {
                            continue;
                        }
                        if (!land.getOwnerVillage().equals(village.getName())) {
                            continue;
                        }
                        if (!village.isBuff()) {
                            continue;
                        }
                        this$0.giveEffects(player, village);
                    }
                }
            }
        }, 0L, 160L);
    }
    
    private PotionEffect getKingdomLevelOneEffect() {
        return new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 300, 0);
    }
    
    private PotionEffect getKingdomLevelTwoEffect() {
        return new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 300, 1);
    }
    
    private PotionEffect getKingdomLevelThreeEffect() {
        return new PotionEffect(PotionEffectType.SPEED, 300, 0);
    }
    
    private PotionEffect getKingdomLevelFiveEffect() {
        return new PotionEffect(PotionEffectType.REGENERATION, 300, 0);
    }
    
    private PotionEffect getKingdomLevelSixEffect() {
        return new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 300, 0);
    }
    
    private PotionEffect getKingdomLevelSevenEffect() {
        return new PotionEffect(PotionEffectType.REGENERATION, 300, 1);
    }
    
    private PotionEffect getKingdomLevelEightEffect() {
        return new PotionEffect(PotionEffectType.FAST_DIGGING, 300, 1);
    }
    
    private PotionEffect getKingdomLevelNineEffect() {
        return new PotionEffect(PotionEffectType.FAST_DIGGING, 300, 2);
    }
    
    private PotionEffect getKingdomLevelTenEffect() {
        return new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 300, 1);
    }
    
    private List<PotionEffect> getApplicablePotionEffects(final Village village, final Player p) {
        if (village == null) {
            return null;
        }
        if (village.getState().equals(VillageState.VILLAGE)) {
            return null;
        }
        final int level = village.getLevel();
        final List<PotionEffect> effects = new ArrayList<PotionEffect>();
        if (level >= 1) {
            effects.add(this.getKingdomLevelOneEffect());
        }
        if (level >= 2) {
            effects.add(this.getKingdomLevelTwoEffect());
        }
        if (level >= 3) {
            effects.add(this.getKingdomLevelThreeEffect());
        }
        if (level >= 5) {
            effects.add(this.getKingdomLevelFiveEffect());
        }
        if (level >= 6) {
            effects.add(this.getKingdomLevelSixEffect());
        }
        if (level >= 7) {
            effects.add(this.getKingdomLevelSevenEffect());
        }
        if (level >= 8) {
            effects.add(this.getKingdomLevelEightEffect());
        }
        if (level >= 9) {
            effects.add(this.getKingdomLevelNineEffect());
        }
        if (level == 10) {
            effects.add(this.getKingdomLevelTenEffect());
            if (p.getWorld().getName().equalsIgnoreCase("kingdom")) {
                effects.add(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 300, 0));
                effects.add(new PotionEffect(PotionEffectType.REGENERATION, 300, 0));
            }
        }
        return effects;
    }
    
    public void giveEffects(final Player player, final Village village) {
        final List<PotionEffect> effects = this.getApplicablePotionEffects(village, player);
        if (effects == null || effects.isEmpty()) {
            return;
        }
        for (final PotionEffect effect : this.getApplicablePotionEffects(village, player)) {
            player.addPotionEffect(effect, true);
        }
        if (village.getLevel() >= 4 && village.getState().equals(VillageState.KINGDOM)) {
            player.getWorld().getName().equalsIgnoreCase("kingdoms");
        }
    }
    
    public void removeEffects(final Player player, final Village village) {
        final List<PotionEffect> effects = this.getApplicablePotionEffects(village, player);
        if (effects == null) {
            return;
        }
        for (final PotionEffect effect : this.getApplicablePotionEffects(village, player)) {
            if (player.hasPotionEffect(effect.getType())) {
                player.removePotionEffect(effect.getType());
            }
        }
    }
}
