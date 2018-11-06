package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.perk.PerkEffect;
import me.crz.minecraft.kingdoms.perk.Perkable;
import me.crz.minecraft.kingdoms.perk.LevelPerk;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
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
            if ((resident.hasVillage() && resident.getVillage().getPerksMap().isEmpty())
                    && (land != null && land.getLandOwner().getPerksMap().isEmpty())) {
                continue;
            }
            giveEffects(player, land, refresh);
        }
    }

    private List<PotionEffect> getApplicablePotionEffects(Player p, OwnedLand land, boolean refresh) {
        List<PerkEffect> effects = new ArrayList<>();
        Resident resident = ResidentManager.getInstance().getResident(p);

        if (resident.hasVillage() && (land == null || (land.isOwnedBy(resident.getVillage()) && land.isBuff()))) {
            if (resident.hasEmpire() && resident.getVillage().getEmpire().isBuff()) {
                effects.addAll(getEffects(p, resident, land, resident.getVillage().getEmpire(), refresh));
            }
            if (resident.getVillage().isBuff()) {
                effects.addAll(getEffects(p, resident, land, resident.getVillage(), refresh));
            }
        }

        Empire empire = EmpireManager.getInstance().getEmpireFromWorld(p.getWorld());
        if (empire != null) {
            for (Perk perk : empire.getPerks()) {
                if (!empire.getPerkData(perk).isActive()) {
                    continue;
                }

                for (PerkEffect effect : perk.getEffects()) {
                    if (effect.getLength() > 0 && refresh) {
                        continue;
                    }

                    if (resident.hasVillage() && effect.getApplyTo() == PerkEffect.EffectInfluenced.FREE) {
                        continue;
                    }

                    if (effect.getApplyIn() != PerkEffect.EffectLocation.EVERYWHERE && effect.getApplyIn() != PerkEffect.EffectLocation.EMPIRE_WORLD) {
                        continue;
                    }
                    effects.add(effect);
                }
            }
        }

        if (land != null && land.getLandOwner() instanceof Village && !land.isOwnedBy(resident.getVillage()) && land.isBuff()) {
            Village landOwner = (Village) land.getLandOwner();
            for (Perk perk : land.getLandOwner().getPerks()) {
                if (!landOwner.getPerkData(perk).isActive()) {
                    continue;
                }

                for (PerkEffect effect : perk.getEffects()) {
                    if (effect.getLength() > 0 && refresh) {
                        continue;
                    }

                    if (resident.hasVillage() && effect.getApplyTo() == PerkEffect.EffectInfluenced.FREE) {
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
                            if (landOwner.isTrusted(resident.getVillage())) {
                                continue;
                            }
                            break;
                        case TRUSTED_SETTLEMENT:
                            if (!landOwner.isTrusted(resident.getVillage())) {
                                continue;
                            }
                            break;
                        case ALLIED_SETTLEMENT:
                            if (!landOwner.isAlly(resident.getVillage())) {
                                continue;
                            }
                            break;
                        case UNCLAIMED:
                            continue;
                    }

                    if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ENEMIES) {
                        if (landOwner.isAlly(resident.getVillage())) {
                            continue;
                        }
                    } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ALLIES) {
                        if (!landOwner.isAlly(resident.getVillage())) {
                            continue;
                        }
                    } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.TRUSTED) {
                        if (!landOwner.isTrusted(resident.getVillage())) {
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

        Map<PotionEffectType, PerkEffect> potionEffects = new LinkedHashMap<>();

        for (PerkEffect effect : effects) {
            PerkEffect perkEffect = potionEffects.get(effect.getType());
            if (perkEffect == null || perkEffect.getStrength() < effect.getStrength() || (perkEffect.getStrength() == effect.getStrength() && perkEffect.getLength() < effect.getLength())) {
                potionEffects.put(effect.getType(), effect);
            }
        }

        return potionEffects.values().stream()
                .map(PerkEffect::getPotionEffect)
                .collect(Collectors.toList());
    }

    private <T extends Perk> Collection<? extends PerkEffect> getEffects(Player p, Resident resident, OwnedLand land, Perkable<T> perkable, boolean refresh) {
        List<PerkEffect> effects = new ArrayList<>();

        Village village = null;
        if (land.getLandOwner() instanceof Village) {
            village = (Village) land.getLandOwner();
        }

        for (T perk : perkable.getPerks()) {
            if (!perkable.getPerkData(perk).isActive()) {
                continue;
            }

            for (PerkEffect effect : perk.getEffects()) {
                if (effect.getLength() > 0 && refresh) {
                    continue;
                }

                if (resident.hasVillage() && effect.getApplyTo() == PerkEffect.EffectInfluenced.FREE) {
                    continue;
                }

                if (land == null) {
                    if (effect.getApplyIn() == PerkEffect.EffectLocation.EMPIRE_WORLD
                            && (!resident.hasEmpire() || !resident.getVillage().getEmpire().equals(EmpireManager.getInstance().getEmpireFromWorld(p.getWorld())))) {
                        continue;
                    }
                    if (effect.getApplyIn() != PerkEffect.EffectLocation.UNCLAIMED) {
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
                            if (land.isOwnedBy(resident.getVillage()) || (village != null && village.isTrusted(resident.getVillage()))) {
                                continue;
                            }
                            break;
                        case ALLIED_SETTLEMENT:
                            if (!land.isOwnedBy(resident.getVillage()) && (village == null || !village.isAlly(resident.getVillage()))) {
                                continue;
                            }
                            break;
                        case TRUSTED_SETTLEMENT:
                            if (!land.isOwnedBy(resident.getVillage()) && (village == null || !village.isTrusted(resident.getVillage()))) {
                                continue;
                            }
                            break;
                    }
                    if (village != null) {
                        if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ENEMIES) {
                            if (land.isOwnedBy(resident.getVillage()) || village.isAlly(resident.getVillage())) {
                                continue;
                            }
                        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ALLIES) {
                            if (!land.isOwnedBy(resident.getVillage()) && !village.isAlly(resident.getVillage())) {
                                continue;
                            }
                        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.TRUSTED) {
                            if (!land.isOwnedBy(resident.getVillage()) && !village.isTrusted(resident.getVillage())) {
                                continue;
                            }
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
        return effects;
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
