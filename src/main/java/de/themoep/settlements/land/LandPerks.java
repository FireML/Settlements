package de.themoep.settlements.land;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.perk.Perk;
import de.themoep.settlements.perk.PerkEffect;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.stream.Collectors;

public class LandPerks {
    private static final String PERK_EFFECT_KEY = "perk_effects";

    public void runTimer() {
        SettlementsPlugin.getScheduler().runTaskTimerAsynchronously(() -> updateEffects(true), 0L, 160L);
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
                player.addPotionEffect(effect.getPotionEffect());
                if (effect.isPermanent()) {
                    activeEffects.add(effect.getIdentifier());
                }
            }
            player.setMetadata(PERK_EFFECT_KEY, new FixedMetadataValue(SettlementsPlugin.getInstance(), String.join(",", activeEffects)));
        }
    }

    private Collection<PerkEffect> getApplicablePotionEffects(Player p, OwnedLand land, boolean refresh) {
        List<PerkEffect> effects = new ArrayList<>();
        Resident resident = ResidentManager.getInstance().getResident(p);

        if (resident.hasSettlement() && (land == null || (land.isOwnedBy(resident.getSettlement()) && land.getMode(PlotMode.buff)))) {
            if (resident.hasEmpire() && resident.getSettlement().getEmpire().getMode(PlotMode.buff)) {
                effects.addAll(getEffects(p, resident, land, resident.getSettlement().getEmpire(), refresh));
            }
            if (resident.getSettlement().getMode(PlotMode.buff)) {
                effects.addAll(getEffects(p, resident, land, resident.getSettlement(), refresh));
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

                    if (resident.hasSettlement()) {
                        if (effect.getApplyTo() == PerkEffect.EffectInfluenced.FREE) {
                            continue;
                        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ALLIES) {
                            if (!empire.isAlly(resident.getSettlement()) && !empire.isAlly(resident.getSettlement().getEmpire())) {
                                continue;
                            }
                        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.TRUSTED) {
                            if (!empire.isTrusted(resident.getSettlement())) {
                                continue;
                            }
                        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.MEMBERS) {
                            if (!empire.isMember(resident)) {
                                continue;
                            }
                        }
                    }
                    if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ENEMIES) {
                        if (empire.isAlly(resident.getSettlement())
                                || (resident.hasEmpire() && empire.isAlly(resident.getSettlement().getEmpire()))
                                || empire.isMember(resident)
                                || empire.isTrusted(resident.getSettlement())) {
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

        if (land != null && land.getLandOwner() instanceof Settlement settlement && !land.isOwnedBy(resident.getSettlement()) && land.getMode(PlotMode.buff)) {
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
                        case ENEMY_SETTLEMENT -> {
                            if (settlement.isTrusted(resident.getSettlement())) {
                                continue;
                            }
                        }
                        case TRUSTED_SETTLEMENT -> {
                            if (!settlement.isTrusted(resident.getSettlement())) {
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
            PerkEffect perkEffect = potionEffects.get(effect.getPotionEffect());
            if (perkEffect == null || perkEffect.getStrength() < effect.getStrength()
                    || (perkEffect.getStrength() == effect.getStrength() && !perkEffect.isPermanent() && (perkEffect.getLength() < effect.getLength() || effect.isPermanent()))) {
                potionEffects.put(effect.getPotionEffect(), effect);
            }
        }

        return potionEffects.values();
    }

    private <T extends Perk> Collection<? extends PerkEffect> getEffects(Player p, Resident resident, OwnedLand land, LandOwner<?, ?> perkable, boolean refresh) {
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

                if (resident.hasSettlement() && effect.getApplyTo() == PerkEffect.EffectInfluenced.FREE) {
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
                        case EMPIRE_WORLD, OWN_SETTLEMENT -> {
                            if (!land.isOwnedBy(perkable)) {
                                continue;
                            }
                        }
                        case ENEMY_SETTLEMENT -> {
                            if (land.isOwnedBy(perkable) || landOwner.isTrusted(perkable)) {
                                continue;
                            }
                        }
                        case ALLIED_SETTLEMENT -> {
                            if (!land.isOwnedBy(perkable) && !landOwner.isAlly(perkable)) {
                                continue;
                            }
                        }
                        case TRUSTED_SETTLEMENT -> {
                            if (!land.isOwnedBy(perkable) && !landOwner.isTrusted(perkable)) {
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

    private boolean appliesTo(PerkEffect effect, Resident resident, LandOwner landOwner) {
        if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ALL) {
            return true;

        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ENEMIES) {
            return !landOwner.isMember(resident) && !landOwner.isAlly(resident);

        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.ALLIES) {
            return landOwner.isMember(resident) || landOwner.isAlly(resident);

        } else if (effect.getApplyTo() == PerkEffect.EffectInfluenced.TRUSTED) {
            return landOwner.isMember(resident) || landOwner.isTrusted(resident.getSettlement());

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
            player.addPotionEffect(effect.getPotionEffect());
            if (effect.isPermanent()) {
                activeEffects.add(effect.getIdentifier());
            }
        }
        player.setMetadata(PERK_EFFECT_KEY, new FixedMetadataValue(SettlementsPlugin.getInstance(), String.join(",", activeEffects)));
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
        player.setMetadata(PERK_EFFECT_KEY, new FixedMetadataValue(SettlementsPlugin.getInstance(), String.join(",", activeEffects)));
    }

    private static Set<String> getActiveEffects(Player player) {
        return player.getMetadata(PERK_EFFECT_KEY).stream()
                .filter(mv -> Objects.equals(mv.getOwningPlugin(), SettlementsPlugin.getInstance()))
                .map(MetadataValue::asString)
                .map(s -> Arrays.asList(s.split(",")))
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    private static String getEffectIdentifier(PotionEffect effect) {
        return effect.getType().getName() + "|" + effect.getAmplifier() + "|" + effect.isAmbient() + "|" + effect.hasParticles();
    }
}
