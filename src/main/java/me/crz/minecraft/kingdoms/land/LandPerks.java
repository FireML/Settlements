package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.village.VillageState;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.Map;
import java.util.stream.Collectors;

import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.Bukkit;

public class LandPerks {
    public LandPerks() {
        super();
    }

    public void runTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(KingdomsPlugin.getInstance(), () -> {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (ResidentManager.getInstance().doesResidentHaveVillage(player)) {
                    final OwnedLand land = LandManager.getInstance().getLand(player.getLocation());
                    if (land == null) {
                        continue;
                    }
                    final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
                    if (village == null) {
                        continue;
                    }
                    if (!land.getOwnerVillage().equals(village)) {
                        continue;
                    }
                    if (!village.isBuff()) {
                        continue;
                    }
                    giveEffects(player, village);
                }
            }
        }, 0L, 160L);
    }

    private List<PotionEffect> getApplicablePotionEffects(final Village village, final Player p) {
        if (village == null) {
            return null;
        }
        if (village.getState().equals(VillageState.VILLAGE)) {
            return null;
        }
        int level = village.getLevel();
        Map<PotionEffectType, Integer> effects = new LinkedHashMap<>();
        switch (level) {
            case 10:
                effects.putIfAbsent(PotionEffectType.DAMAGE_RESISTANCE, 1);
                if (p.getWorld().getName().equalsIgnoreCase(KingdomsPlugin.getInstance().getKingdomsWorld())) {
                    effects.putIfAbsent(PotionEffectType.INCREASE_DAMAGE, 0);
                    effects.putIfAbsent(PotionEffectType.REGENERATION, 0);
                }
            case 9:
                effects.putIfAbsent(PotionEffectType.FAST_DIGGING, 2);
            case 8:
                effects.putIfAbsent(PotionEffectType.FAST_DIGGING, 1);
            case 7:
                effects.putIfAbsent(PotionEffectType.REGENERATION, 1);
            case 6:
                effects.putIfAbsent(PotionEffectType.DAMAGE_RESISTANCE, 0);
            case 5:
                effects.putIfAbsent(PotionEffectType.REGENERATION, 0);
            case 4:
            case 3:
                effects.putIfAbsent(PotionEffectType.SPEED, 0);
            case 2:
                effects.putIfAbsent(PotionEffectType.INCREASE_DAMAGE, 1);
            case 1:
                effects.putIfAbsent(PotionEffectType.INCREASE_DAMAGE, 0);
        }
        return effects.entrySet().stream().map(e -> new PotionEffect(e.getKey(), 300, e.getValue())).collect(Collectors.toList());
    }

    public void giveEffects(final Player player, final Village village) {
        final List<PotionEffect> effects = this.getApplicablePotionEffects(village, player);
        if (effects == null || effects.isEmpty()) {
            return;
        }
        for (final PotionEffect effect : this.getApplicablePotionEffects(village, player)) {
            player.addPotionEffect(effect, true);
        }
        if (village.getLevel() >= 4 && village.getState() == VillageState.KINGDOM) {
            player.getWorld().getName().equalsIgnoreCase("kingdoms"); // TODO: wat?
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
