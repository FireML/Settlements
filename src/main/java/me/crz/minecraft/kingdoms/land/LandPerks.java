package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Pair;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillagePerk;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LandPerks {
    public LandPerks() {
        super();
    }

    public void runTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(KingdomsPlugin.getInstance(), () -> updateEffects(false), 0L, 160L);
    }

    /**
     * Update the effects for all players at the location that they are in
     * @param refresh Whether or not the updating is a result of a refresh or just granting a new effect.
     *                Refreshing will not give effects with a non-permanent duration.
     */
    public void updateEffects(boolean refresh) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            OwnedLand land = LandManager.getInstance().getLand(player.getLocation());
            Resident resident = ResidentManager.getInstance().getResident(player);
            if ((resident.getVillage().isBuff() || resident.getVillage().getPerksMap().isEmpty())
                    && (land != null && (!land.getOwnerVillage().isBuff() || land.getOwnerVillage().getPerksMap().isEmpty()))) {
                continue;
            }
            giveEffects(player, land, refresh);
        }
    }

    private List<PotionEffect> getApplicablePotionEffects(Player p, OwnedLand land, boolean refresh) {
        List<VillagePerk.PerkEffect> effects = new ArrayList<>();
        Resident resident = ResidentManager.getInstance().getResident(p);

        for (VillagePerk perk : resident.getVillage().getPerks()) {
            for (VillagePerk.PerkEffect effect : perk.getEffects()) {
                if (!resident.getVillage().getPerkData(perk).isActive()) {
                    continue;
                }

                if (effect.getLength() > 0 && refresh) {
                    continue;
                }

                if (resident.hasVillage() && effect.getApplyTo() == VillagePerk.EffectInfluenced.FREE) {
                    continue;
                }

                if (land == null) {
                    if (effect.getApplyIn() != VillagePerk.EffectLocation.UNCLAIMED) {
                        continue;
                    }
                } else {
                    switch (effect.getApplyIn()) {
                        case PLOT:
                            if (!land.isPlotOwner(p.getUniqueId())) {
                                continue;
                            }
                            break;
                        case PLOT_FRIENDS:
                            if (!land.isPlot() || !resident.getFriends().contains(land.getPlotOwner())) {
                                continue;
                            }
                            break;
                        case OWN_SETTLEMENT:
                            if (!land.isOwnedBy(resident.getVillage())) {
                                continue;
                            }
                            break;
                        case ENEMY_SETTLEMENT:
                            if (land.isOwnedBy(resident.getVillage()) || land.getOwnerVillage().isAlly(resident.getVillage())) {
                                continue;
                            }
                            break;
                        case ALLIED_SETTLEMENT:
                            if (!land.isOwnedBy(resident.getVillage()) && !land.getOwnerVillage().isAlly(resident.getVillage())) {
                                continue;
                            }
                            break;
                    }
                    if (effect.getApplyTo() == VillagePerk.EffectInfluenced.ENEMIES) {
                        if (land.isOwnedBy(resident.getVillage()) || land.getOwnerVillage().isAlly(resident.getVillage())) {
                            continue;
                        }
                    } else if (effect.getApplyTo() == VillagePerk.EffectInfluenced.ALLIES) {
                        if (!land.isOwnedBy(resident.getVillage()) && !land.getOwnerVillage().isAlly(resident.getVillage())) {
                            continue;
                        }
                    }
                }

                if (resident.hasVillage()) {
                    VillageRank rank = effect.getApplyTo().getRank();
                    if (rank != null && resident.getVillageRank().below(rank)) {
                        continue;
                    }
                }

                effects.add(effect);
            }
        }

        if (land != null && !land.isOwnedBy(resident.getVillage())) {
            for (VillagePerk perk : land.getOwnerVillage().getPerks()) {
                if (!land.getOwnerVillage().getPerkData(perk).isActive()) {
                    continue;
                }

                for (VillagePerk.PerkEffect effect : perk.getEffects()) {
                    if (effect.getLength() > 0 && refresh) {
                        continue;
                    }

                    if (resident.hasVillage() && effect.getApplyTo() == VillagePerk.EffectInfluenced.FREE) {
                        continue;
                    }

                    switch (effect.getApplyIn()) {
                        case PLOT:
                            if (!land.isPlotOwner(p.getUniqueId())) {
                                continue;
                            }
                            break;
                        case PLOT_FRIENDS:
                            if (!land.isPlot() || !resident.getFriends().contains(land.getPlotOwner())) {
                                continue;
                            }
                            break;
                        case ENEMY_SETTLEMENT:
                            if (land.getOwnerVillage().isAlly(resident.getVillage())) {
                                continue;
                            }
                            break;
                        case ALLIED_SETTLEMENT:
                            if (!land.getOwnerVillage().isAlly(resident.getVillage())) {
                                continue;
                            }
                            break;
                        case UNCLAIMED:
                            continue;
                    }

                    if (effect.getApplyTo() == VillagePerk.EffectInfluenced.ENEMIES) {
                        if (land.getOwnerVillage().isAlly(resident.getVillage())) {
                            continue;
                        }
                    } else if (effect.getApplyTo() == VillagePerk.EffectInfluenced.ALLIES) {
                        if (!land.getOwnerVillage().isAlly(resident.getVillage())) {
                            continue;
                        }
                    }

                    if (resident.hasVillage()) {
                        VillageRank rank = effect.getApplyTo().getRank();
                        if (rank != null && resident.getVillageRank().below(rank)) {
                            continue;
                        }
                    }

                    effects.add(effect);
                }
            }
        }

        Map<PotionEffectType, VillagePerk.PerkEffect> potionEffects = new LinkedHashMap<>();

        for (VillagePerk.PerkEffect effect : effects) {
            VillagePerk.PerkEffect perkEffect = potionEffects.get(effect.getType());
            if (perkEffect == null || perkEffect.getStrength() < effect.getStrength() || (perkEffect.getStrength() == effect.getStrength() && perkEffect.getLength() < effect.getLength())) {
                potionEffects.put(effect.getType(), effect);
            }
        }

        return potionEffects.values().stream()
                .map(VillagePerk.PerkEffect::getPotionEffect)
                .collect(Collectors.toList());
    }

    public void giveEffects(Player player, OwnedLand land, boolean refresh) {
        List<PotionEffect> effects = getApplicablePotionEffects(player, land, refresh);
        if (effects == null || effects.isEmpty()) {
            return;
        }
        for (PotionEffect effect : getApplicablePotionEffects(player, land, refresh)) {
            player.addPotionEffect(effect, true);
        }
    }

    public void removeEffects(Player player, OwnedLand land, boolean refresh) {
        List<PotionEffect> effects = getApplicablePotionEffects(player, land, refresh);
        if (effects == null) {
            return;
        }
        for (PotionEffect effect : getApplicablePotionEffects(player, land, refresh)) {
            if (player.hasPotionEffect(effect.getType())) {
                player.removePotionEffect(effect.getType());
            }
        }
    }
}
