package de.themoep.settlements.land;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.perk.Perk;
import de.themoep.settlements.perk.PerkEffect;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import uk.firedev.daisylib.utils.Scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class LandPerks {

    private static final Map<UUID, String> PERK_EFFECTS = new HashMap<>();

    public void runTimer() {
        Scheduling.runTaskTimer(() -> updateEffects(true), 1L, 160L);
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
                PotionEffect potionEffect = effect.getPotionEffect();
                if (potionEffect == null) {
                    SettlementsPlugin.getInstance().getLogging().warn("PerkEffect " + effect.getIdentifier() + " does not have a valid PotionEffect.");
                    continue;
                }
                player.addPotionEffect(effect.getPotionEffect());
                if (effect.isPermanent()) {
                    activeEffects.add(effect.getIdentifier());
                }
            }
            PERK_EFFECTS.put(player.getUniqueId(), String.join(",", activeEffects));
        }
    }

    private Collection<PerkEffect> getApplicablePotionEffects(Player p, OwnedLand land, boolean refresh) {
        List<PerkEffect> effects = new ArrayList<>();
        Resident resident = ResidentManager.getInstance().getResident(p);

        if (resident.hasSettlement() && (land == null || (land.isOwnedBy(resident.getSettlement()) && land.getMode(PlotMode.buff)))) {
            if (resident.getSettlement().getMode(PlotMode.buff)) {
                effects.addAll(getEffects(p, resident, land, resident.getSettlement(), refresh));
            }
        }

        if (land != null && land.getSettlement() instanceof Settlement settlement && !land.isOwnedBy(resident.getSettlement()) && land.getMode(PlotMode.buff)) {
            for (Perk perk : settlement.getPerks()) {
                if (!settlement.getPerkData(perk).isActive()) {
                    continue;
                }

                for (PerkEffect effect : perk.getEffects()) {
                    if (!effect.isPermanent() && refresh) {
                        continue;
                    }

                    if (resident.hasSettlement() && effect.getApplyTo() == PerkEffect.EffectInfluenced.FREE) {
                        continue;
                    }

                    switch (effect.getApplyIn()) {
                        case PLOT -> {
                            if (!land.isPlotOwner(p.getUniqueId())) {
                                continue;
                            }
                        }
                        case PLOT_FRIENDS -> {
                            if (!land.isPlot() || !resident.isFriend(land.getPlotOwner())) {
                                continue;
                            }
                        }
                        case ALLIED_SETTLEMENT -> {
                            if (!settlement.isAlly(resident.getSettlement())) {
                                continue;
                            }
                        }
                        case UNCLAIMED -> {
                            continue;
                        }
                    }

                    if (appliesTo(effect, resident, settlement)) {
                        effects.add(effect);
                    }
                }
            }
        }

        Map<PotionEffect, PerkEffect> potionEffects = new LinkedHashMap<>();

        for (PerkEffect effect : effects) {
            PotionEffect potionEffect = effect.getPotionEffect();
            if (potionEffect == null) {
                continue;
            }
            PerkEffect perkEffect = potionEffects.get(potionEffect);
            if (perkEffect == null || perkEffect.getStrength() < effect.getStrength()
                    || (perkEffect.getStrength() == effect.getStrength() && !perkEffect.isPermanent() && (perkEffect.getLength() < effect.getLength() || effect.isPermanent()))) {
                potionEffects.put(potionEffect, effect);
            }
        }

        return potionEffects.values();
    }

    private <T extends Perk> Collection<? extends PerkEffect> getEffects(Player p, Resident resident, OwnedLand land, Settlement perkable, boolean refresh) {
        List<PerkEffect> effects = new ArrayList<>();

        Settlement landOwner = null;
        if (land != null) {
            landOwner = land.getSettlement();
        }

        for (Perk perk : perkable.getPerks()) {
            if (!perkable.getPerkData(perk).isActive()) {
                continue;
            }

            for (PerkEffect effect : perk.getEffects()) {
                if (!effect.isPermanent() && refresh) {
                    continue;
                }

                if (resident.hasSettlement() && effect.getApplyTo() == PerkEffect.EffectInfluenced.FREE) {
                    continue;
                }

                if (land == null) {
                    if (effect.getApplyIn() != PerkEffect.EffectLocation.UNCLAIMED) {
                        continue;
                    }
                } else {
                    switch (effect.getApplyIn()) {
                        case PLOT -> {
                            if (!land.isPlotOwner(p.getUniqueId())) {
                                continue;
                            }
                        }
                        case PLOT_FRIENDS -> {
                            if (!land.isPlot() || !resident.isFriend(land.getPlotOwner())) {
                                continue;
                            }
                        }
                        case OWN_SETTLEMENT -> {
                            if (!land.isOwnedBy(perkable)) {
                                continue;
                            }
                        }
                        case ALLIED_SETTLEMENT -> {
                            if (!land.isOwnedBy(perkable) && !landOwner.isAlly(perkable)) {
                                continue;
                            }
                        }
                    }
                }

                if (appliesTo(effect, resident, perkable)) {
                    effects.add(effect);
                }
            }
        }
        return effects;
    }

    private boolean appliesTo(PerkEffect effect, Resident resident, Settlement landOwner) {
        if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ALL) {
            return true;

        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ENEMIES) {
            return !landOwner.isMember(resident) && !landOwner.isAlly(resident);

        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ALLIES) {
            return landOwner.isMember(resident) || landOwner.isAlly(resident);

        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.MEMBERS) {
            return landOwner.isMember(resident);

        } else if (landOwner.isMember(resident)) {
            SettlementRank rank = effect.getApplyTo().getRank();
            if (rank != null) {
                return resident.getSettlementRank().atLeast(rank);
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
            PotionEffect potionEffect = effect.getPotionEffect();
            if (potionEffect == null) {
                SettlementsPlugin.getInstance().getLogging().warn("PerkEffect " + effect.getIdentifier() + " does not have a valid PotionEffect.");
                continue;
            }
            player.addPotionEffect(potionEffect);
            if (effect.isPermanent()) {
                activeEffects.add(effect.getIdentifier());
            }
        }
        PERK_EFFECTS.put(player.getUniqueId(), String.join(",", activeEffects));
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
        PERK_EFFECTS.put(player.getUniqueId(), String.join(",", activeEffects));
    }

    private static Set<String> getActiveEffects(Player player) {
        String effects = PERK_EFFECTS.get(player.getUniqueId());
        return effects == null ? Set.of() : Set.of(effects.split(","));
    }

    private static String getEffectIdentifier(PotionEffect effect) {
        return effect.getType().getKey() + "|" + effect.getAmplifier() + "|" + effect.isAmbient() + "|" + effect.hasParticles();
    }

}
