package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.perk.PerkEffect;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class LandPerks {
    private static final String PERK_EFFECT_KEY = "perk_effects";

    public LandPerks() {
        super();
    }

    public void runTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(KingdomsPlugin.getInstance(), () -> updateEffects(true), 0L, 160L);
    }

    /**
     * Update the effects for all players at the location that they are in
     * @param refresh Whether or not the updating is a result of a refresh or just granting a new effect.
     *                Refreshing will not give effects with a non-permanent duration.
     */
    public void updateEffects(boolean refresh) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            OwnedLand land = LandManager.getInstance().getLand(player.getLocation());

            Collection<PerkEffect> applicableEffects = getApplicablePotionEffects(player, land, refresh);
            Set<String> activeEffects = getActiveEffects(player);
            Set<String> toRemove = new HashSet<>(activeEffects);
            applicableEffects.forEach(e -> toRemove.remove(e.getIdentifier()));

            for (PotionEffect effect : player.getActivePotionEffects()) {
                String identifier = getEffectIdentifier(effect);
                if (toRemove.contains(identifier)) {
                    player.removePotionEffect(effect.getType());
                    activeEffects.remove(identifier);
                }
            }

            for (PerkEffect effect : applicableEffects) {
                player.addPotionEffect(effect.getPotionEffect(), true);
                if (effect.isPermanent()) {
                    activeEffects.add(effect.getIdentifier());
                }
            }
            player.setMetadata(PERK_EFFECT_KEY, new FixedMetadataValue(KingdomsPlugin.getInstance(), String.join(",", activeEffects)));
        }
    }

    private Collection<PerkEffect> getApplicablePotionEffects(Player p, OwnedLand land, boolean refresh) {
        List<PerkEffect> effects = new ArrayList<>();
        Resident resident = ResidentManager.getInstance().getResident(p);

        if (resident.hasVillage() && (land == null || (land.isOwnedBy(resident.getVillage()) && land.getMode(PlotMode.buff)))) {
            if (resident.hasEmpire() && resident.getVillage().getEmpire().getMode(PlotMode.buff)) {
                effects.addAll(getEffects(p, resident, land, resident.getVillage().getEmpire(), refresh));
            }
            if (resident.getVillage().getMode(PlotMode.buff)) {
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
                    if (!effect.isPermanent() && refresh) {
                        continue;
                    }

                    if (resident.hasVillage()) {
                        if (effect.getApplyTo() == PerkEffect.EffectInfluenced.FREE) {
                            continue;
                        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ALLIES) {
                            if (!empire.isAlly(resident.getVillage()) && !empire.isAlly(resident.getVillage().getEmpire())) {
                                continue;
                            }
                        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.TRUSTED) {
                            if (!empire.isTrusted(resident.getVillage())) {
                                continue;
                            }
                        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.MEMBERS) {
                            if (!empire.isMember(resident)) {
                                continue;
                            }
                        }
                    }
                    if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ENEMIES) {
                        if (empire.isAlly(resident.getVillage())
                                || (resident.hasEmpire() && empire.isAlly(resident.getVillage().getEmpire()))
                                || empire.isMember(resident)
                                || empire.isTrusted(resident.getVillage())) {
                            continue;
                        }
                    }

                    if (effect.getApplyIn() != PerkEffect.EffectLocation.EVERYWHERE && effect.getApplyIn() != PerkEffect.EffectLocation.EMPIRE_WORLD) {
                        continue;
                    }
                    effects.add(effect);
                }
            }
        }

        if (land != null && land.getLandOwner() instanceof Village && !land.isOwnedBy(resident.getVillage()) && land.getMode(PlotMode.buff)) {
            Village landOwner = (Village) land.getLandOwner();
            for (Perk perk : landOwner.getPerks()) {
                if (!landOwner.getPerkData(perk).isActive()) {
                    continue;
                }

                for (PerkEffect effect : perk.getEffects()) {
                    if (!effect.isPermanent() && refresh) {
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

                    if (appliesTo(effect, resident, landOwner)) {
                        effects.add(effect);
                    }
                }
            }
        }

        Map<PotionEffectType, PerkEffect> potionEffects = new LinkedHashMap<>();

        for (PerkEffect effect : effects) {
            PerkEffect perkEffect = potionEffects.get(effect.getType());
            if (perkEffect == null || perkEffect.getStrength() < effect.getStrength()
                    || (perkEffect.getStrength() == effect.getStrength() && !perkEffect.isPermanent() && (perkEffect.getLength() < effect.getLength() || effect.isPermanent()))) {
                potionEffects.put(effect.getType(), effect);
            }
        }

        return potionEffects.values();
    }

    private <T extends Perk> Collection<? extends PerkEffect> getEffects(Player p, Resident resident, OwnedLand land, LandOwner<?> perkable, boolean refresh) {
        List<PerkEffect> effects = new ArrayList<>();

        LandOwner landOwner = null;
        if (land != null) {
            landOwner = land.getLandOwner();
        }

        for (Perk perk : perkable.getPerks()) {
            if (!perkable.getPerkData(perk).isActive()) {
                continue;
            }

            for (PerkEffect effect : perk.getEffects()) {
                if (!effect.isPermanent() && refresh) {
                    continue;
                }

                if (resident.hasVillage() && effect.getApplyTo() == PerkEffect.EffectInfluenced.FREE) {
                    continue;
                }

                if (land == null) {
                    if (effect.getApplyIn() == PerkEffect.EffectLocation.EMPIRE_WORLD
                            && !perkable.equals(EmpireManager.getInstance().getEmpireFromWorld(p.getWorld()))) {
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
                        case EMPIRE_WORLD:
                        case OWN_SETTLEMENT:
                            if (!land.isOwnedBy(perkable)) {
                                continue;
                            }
                            break;
                        case ENEMY_SETTLEMENT:
                            if (land.isOwnedBy(perkable) || landOwner.isTrusted(perkable)) {
                                continue;
                            }
                            break;
                        case ALLIED_SETTLEMENT:
                            if (!land.isOwnedBy(perkable) && !landOwner.isAlly(perkable)) {
                                continue;
                            }
                            break;
                        case TRUSTED_SETTLEMENT:
                            if (!land.isOwnedBy(perkable) && !landOwner.isTrusted(perkable)) {
                                continue;
                            }
                            break;
                    }
                }

                if (appliesTo(effect, resident, perkable)) {
                    effects.add(effect);
                }
            }
        }
        return effects;
    }

    private boolean appliesTo(PerkEffect effect, Resident resident, LandOwner landOwner) {
        if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ALL) {
            return true;

        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ENEMIES) {
            return !landOwner.isMember(resident) && !landOwner.isAlly(resident);

        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ALLIES) {
            return landOwner.isMember(resident) || landOwner.isAlly(resident);

        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.TRUSTED) {
            return landOwner.isMember(resident) || landOwner.isTrusted(resident.getVillage());

        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.MEMBERS) {
            return landOwner.isMember(resident);

        } else if (landOwner.isMember(resident)) {
            VillageRank rank = effect.getApplyTo().getRank();
            if (rank != null) {
                return resident.getVillageRank().atLeast(rank);
            }
        }
        return false;
    }

    public void giveEffects(Player player, OwnedLand land, boolean refresh) {
        Collection<PerkEffect> effects = getApplicablePotionEffects(player, land, refresh);
        if (effects.isEmpty()) {
            return;
        }
        Set<String> activeEffects = getActiveEffects(player);
        for (PerkEffect effect : effects) {
            player.addPotionEffect(effect.getPotionEffect(), true);
            if (effect.isPermanent()) {
                activeEffects.add(effect.getIdentifier());
            }
        }
        player.setMetadata(PERK_EFFECT_KEY, new FixedMetadataValue(KingdomsPlugin.getInstance(), String.join(",", activeEffects)));
    }

    public void removeEffects(Player player, OwnedLand land, boolean refresh) {
        Set<String> activeEffects = getActiveEffects(player);
        Set<String> toRemove = new HashSet<>(activeEffects);
        getApplicablePotionEffects(player, land, refresh).forEach(e -> toRemove.remove(e.getIdentifier()));
        for (PotionEffect effect : player.getActivePotionEffects()) {
            String identifier = getEffectIdentifier(effect);
            if (toRemove.contains(identifier)) {
                player.removePotionEffect(effect.getType());
                activeEffects.remove(identifier);
            }
        }
        player.setMetadata(PERK_EFFECT_KEY, new FixedMetadataValue(KingdomsPlugin.getInstance(), String.join(",", activeEffects)));
    }

    private static Set<String> getActiveEffects(Player player) {
        return player.getMetadata(PERK_EFFECT_KEY).stream()
                .filter(mv -> Objects.equals(mv.getOwningPlugin(), KingdomsPlugin.getInstance()))
                .map(MetadataValue::asString)
                .map(s -> Arrays.asList(s.split(",")))
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    private static String getEffectIdentifier(PotionEffect effect) {
        return effect.getType().getName() + "|" + effect.getAmplifier() + "|" + effect.isAmbient() + "|" + effect.hasParticles();
    }
}
