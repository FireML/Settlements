package de.themoep.settlements.integration.citizens;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.empire.gui.SettingsGUI;
import de.themoep.settlements.empire.gui.WorldGUI;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.LandownerLevel;
import de.themoep.settlements.land.events.LandClaimedEvent;
import de.themoep.settlements.land.events.LandUnclaimEvent;
import de.themoep.settlements.land.events.LandownerLeveledEvent;
import de.themoep.settlements.land.gui.BankGUI;
import de.themoep.settlements.land.gui.ElderGUI;
import de.themoep.settlements.land.gui.UpgradeGUI;
import de.themoep.settlements.npc.NpcConfig;
import de.themoep.settlements.npc.NpcData;
import de.themoep.settlements.npc.NpcManager;
import de.themoep.settlements.npc.NpcType;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.land.events.LandownerAbandonedEvent;
import de.themoep.settlements.land.events.LandownerCreatedEvent;
import de.themoep.settlements.settlement.gui.ManagementGUI;
import de.themoep.settlements.settlement.gui.OutpostsGUI;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import de.themoep.settlements.world.SimpleLocation;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.trait.versioned.VillagerTrait;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class CitizensIntegration implements Listener, ConfigMessages {

    private static final Random RANDOM = new Random();

    public CitizensIntegration() {
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SettlementsTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(AuctionTaxTrait.class));

        SubCommand settlementCommand = CommandManager.getInstance().getCommand("settlement");
        settlementCommand.registerCommand(new NpcCommand(this, settlementCommand, Resident::getSettlement));
        SettlementsPlugin.getInstance().getServer().getPluginManager().registerEvents(this, SettlementsPlugin.getInstance());
    }

    public String getCitizensVersion() {
        return CitizensAPI.getPlugin().getDescription().getVersion();
    }

    @EventHandler
    public void onSettlementCreate(LandownerCreatedEvent event) {
        for (NpcType npc : event.getLandOwner().getLevelInfo().getNPCs()) {
            unlockNpc(event.getLandOwner(), npc);
            if (event.getLandOwner().getHome() != null) {
                createNpc(event.getLandOwner(), npc, event.getLandOwner().getHome().toBukkitLocation());
                getMessage("new-npc-available").tell(event.getLandOwner(),
                        "npc", npc.getTag(),
                        "type", npc.getKey(),
                        "state", event.getLandOwner().getStateTag());
            }
        }
    }

    @EventHandler
    public void onCaimed(LandClaimedEvent event) {
        for (NpcType npc : event.getLandOwner().getLevelInfo().getNPCs()) {
            handle(event, npc);
            getMessage("new-npc-available").tell(event.getLandOwner(),
                    "npc", npc.getTag(),
                    "type", npc.getKey(),
                    "state", event.getLandOwner().getStateTag());
        }
    }

    private void handle(LandClaimedEvent event, NpcType type) {
        NpcData npcData = event.getLandOwner().getNpcData(type);
        if (npcData.isUnlocked() && getNpc(event.getLandOwner(), type) == null) {
            Location location;
            if (event.getLandOwner().getHome() != null) {
                location = event.getLandOwner().getHome().toBukkitLocation();
            } else {
                location = event.getLand().getCenterBukkitLocation();
            }
            createNpc(event.getLandOwner(), type, location);
        }
    }

    public void createNpc(LandOwner<?, ?> landOwner, NpcType type, Location location) {
        createNpc(landOwner, type, NpcManager.getInstance().getConfig(landOwner, type), location);
    }

    void createNpc(LandOwner<?, ?> landOwner, NpcType type, NpcConfig config, Location location) {
        if (config == null) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Tried to create " + type + " NPC for " + landOwner.getKey() + " " + landOwner.getName() + " but there was no config specified for that NPC type?");
            return;
        }
        NPC npc = getNpc(landOwner, type);
        if (npc != null) {
            if (npc.isSpawned()) {
                npc.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
            } else {
                npc.spawn(location);
            }
            return;
        }

        NpcData npcData = landOwner.getNpcData(type);

        EntityType entityType = npcData.getNpcSkinName().isEmpty() ? config.getEntityType() : EntityType.PLAYER;
        npc = CitizensAPI.getNPCRegistry().createNPC(entityType, config.getName());

        npc.setProtected(true);

        SettlementsTrait typeTrait = npc.getOrAddTrait(SettlementsTrait.class);
        typeTrait.type = type;
        typeTrait.ownerType = landOwner.getDbName();
        typeTrait.ownerId = (int) landOwner.getId().value();
        typeTrait.messages = config.getMessages();
        npc.addTrait(typeTrait);

        LookClose lookTrait = npc.getOrAddTrait(LookClose.class);
        lookTrait.lookClose(true);

        Anchors anchors = npc.getOrAddTrait(Anchors.class);
        anchors.addAnchor("home", location);

         /* TODO: Custom waypoint provider to check claims
        Waypoints waypoints = npc.getOrAddTrait(Waypoints.class);
        waypoints.setWaypointProvider("wanderclaims");
        if (waypoints.getCurrentProvider() instanceof WanderWaypointProvider) {
            WanderWaypointProvider wander = (WanderWaypointProvider) waypoints.getCurrentProvider();
            wander.setXYRange(2, 2);
            wander.addRegionCentre(location);
        } */

        if (npcData.getNpcSkinName().isEmpty()) {
            applyNpcTraits(npc, config);
        } else {
            npc.getOrAddTrait(SkinTrait.class)
                    .setSkinName(npcData.getNpcSkinName(), true);
        }

        npcData.setNpcId(npc.getId());
        if (!npcData.isUnlocked()) {
            npcData.setLevel(1);
        }
        landOwner.addNpc(npcData);
        npcData.save();
        npc.spawn(location);
        //npc.save(); // TODO: Do we need to save them?
    }

    void applyNpcTraits(NPC npc, NpcConfig config) {
        switch (config.getEntityType()) {
            case PLAYER: {
                SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);

                String skinName = config.getSkinName();
                String texture = config.getSkinTextures();
                String signature = config.getSkinSignature();
                try {
                    if (!texture.isEmpty() && !signature.isEmpty() && !skinName.isEmpty()) {
                        skinTrait.setSkinPersistent(skinName, signature, texture);
                    } else if (!texture.isEmpty() && !signature.isEmpty()) {
                        skinTrait.setTexture(texture, signature);
                    } else if (!skinName.isEmpty()) {
                        skinTrait.setSkinName(skinName);
                    }
                } catch (IllegalArgumentException e) {
                    SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Invalid texture data specified! (name: " + skinName + ", texture: " + texture + ", signature: " + signature + ") " + e.getMessage());
                }
                break;
            }
            case VILLAGER: {
                npc.getOrAddTrait(VillagerTrait.class).setType(config.getVillagerType());
                npc.getOrAddTrait(VillagerProfession.class).setProfession(config.getVillagerProfession());
            }
        }
    }

    public static String getName(String type) {
        return ConfigManager.getInstance().getNpcConfig().getString(type + ".name");
    }

    public static EntityType getType(String type) {
        try {
            return EntityType.valueOf(ConfigManager.getInstance().getNpcConfig().getString(type + ".type").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return EntityType.VILLAGER;
        }
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        handleClick(event);
    }

    @EventHandler
    public void onLeftClick(NPCLeftClickEvent event) {
        handleClick(event);
    }

    private void handleClick(NPCClickEvent event) {
        Resident resident = ResidentManager.getInstance().getResident(event.getClicker());
        NPC npc = event.getNPC();

        SettlementsTrait typeTrait = npc.getTraitNullable(SettlementsTrait.class);
        if (typeTrait != null) {
            LandOwner<?, ?> landOwner = typeTrait.getOwner();
            if (landOwner == null) {
                return;
            }

            if (landOwner.useGuestRank() && landOwner.getRank(resident) == landOwner.getDefaultRank()) {
                getError("rules-not-accepted").tell(resident, "state", landOwner.getStateTag(), "name", landOwner.getName());
                event.setCancelled(true);
                return;
            }

            switch (typeTrait.type) {
                case MANAGER: {
                    if (landOwner.hasPermission(resident, "settings")) {
                        if (landOwner instanceof Settlement) {
                            new ManagementGUI(resident).open();
                        } else if (landOwner instanceof Empire) {
                            new SettingsGUI(resident).open();
                        }
                    } else {
                        event.setCancelled(true);
                        getError("no-permission").tell(resident);
                    }
                    return;
                }
                case QUARTER_MASTER: {
                    if (landOwner.hasPermission(resident, "upgrade")) {
                        new UpgradeGUI(resident, landOwner).open();
                    } else {
                        event.setCancelled(true);
                        getError("no-permission").tell(resident);
                    }
                    return;
                }
                case BANKER: {
                    if (landOwner.hasPermission(resident, "bank") || landOwner.hasUnlocked("donation")) {
                        new BankGUI(resident, landOwner).open();
                    } else {
                        event.setCancelled(true);
                        getError("no-permission").tell(resident);
                    }
                    return;
                }
                case BLACK_MARKET: {
                    if (landOwner.hasPermission(resident, "shop")) {
                        if (landOwner instanceof Settlement) {
                            new de.themoep.settlements.settlement.gui.ShopGUI(resident).open();
                        } else if (landOwner instanceof Empire) {
                            new de.themoep.settlements.empire.gui.ShopGUI(resident).open();
                        }
                    } else {
                        event.setCancelled(true);
                        getError("no-permission").tell(resident);
                    }
                    return;
                }
                case TRAVELING_MERCHANT: {
                    if (landOwner instanceof Settlement) {
                        if (landOwner.isMember(resident) || landOwner.getAccess() == LandAccess.PUBLIC) {
                            new OutpostsGUI(resident, (Settlement) landOwner).open();
                        }
                    } else if (landOwner instanceof Empire) {
                        if (((Empire) landOwner).canAccess(resident)) {
                            new WorldGUI(resident, (Empire) landOwner).open();
                        }
                    }
                    return;
                }
                case ELDER: {
                    new ElderGUI(resident, landOwner).open();
                    return;
                }
                case AUCTIONEER: {
                    // Handled in auction plugin
                    return;
                }
                case BLACKSMITH: {
                    // Handled in refine plugin
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onLeveled(LandownerLeveledEvent event) {
        LandownerLevel level = event.getLeveled().getLevelInfo();
        if (level != null) {
            for (NpcType npc : level.getNPCs()) {
                unlockNpc(event.getLeveled(), npc);
                if (event.getLeveled().getHome() != null) {
                    createNpc(event.getLeveled(), npc, event.getLeveled().getHome().toBukkitLocation());
                    getMessage("new-npc-available").tell(event.getLeveled(),
                            "npc", npc.getTag(),
                            "type", npc.getKey(),
                            "state", event.getLeveled().getStateTag());
                } else {
                    getMessage("new-npc-unlocked").tell(event.getLeveled(),
                            "npc", npc.getTag(),
                            "type", npc.getKey(),
                            "state", event.getLeveled().getStateTag());
                }
            }
        }
    }

    private void unlockNpc(LandOwner<?, ?> landOwner, NpcType type) {
        NpcData npcData = landOwner.getNpcData(type);
        if (!npcData.isUnlocked()) {
            npcData.setLevel(1);
            npcData.save();
        }
        landOwner.addNpc(npcData);
    }

    @EventHandler
    public void onSettlementDisband(LandownerAbandonedEvent event) {
        for (NpcData npcData : event.getLandOwner().getNpcData()) {
            if (npcData.getNpcId() > -1) {
                NPC npc = CitizensAPI.getNPCRegistry().getById(npcData.getNpcId());
                if (npc != null) {
                    SettlementsPlugin.logDebug("Despawning NPC " + npcData.getType() + " due to abandonment of " + event.getLandOwner().getStateTag() + " " + event.getLandOwner().getName());
                    npc.despawn();
                    CitizensAPI.getNPCRegistry().deregister(npc);
                    if (npc.getEntity() != null) {
                        npc.getEntity().remove();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onUnclaim(LandUnclaimEvent event) {
        for (NpcData npcData : event.getLandOwner().getNpcData()) {
            if (npcData.getNpcId() > -1) {
                NPC npc = CitizensAPI.getNPCRegistry().getById(npcData.getNpcId());
                if (npc != null && npc.isSpawned() && event.getLand().contains(new SimpleLocation(npc.getEntity().getLocation()))) {
                    if (event.getLand().getLandOwner().getHome() != null) {
                        npc.teleport(event.getLand().getLandOwner().getHome().toBukkitLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                    } else {
                        SettlementsPlugin.logDebug("Despawning NPC " + npcData.getType() + " due to unclaim of " + event.getLandOwner().getStateTag() + " " + event.getLandOwner().getName() + " land at " + event.getLand().getKey());
                        npc.despawn();
                        if (npc.getEntity() != null) {
                            npc.getEntity().remove();
                        }
                    }
                }
            }
        }
    }

    public NPC getNpc(LandOwner<?, ?> landOwner, NpcType type) {
        NpcData npcData = landOwner.getNpcData(type);
        if (npcData == null || npcData.getNpcId() < 0) {
            return null;
        }
        try {
            return CitizensAPI.getNPCRegistry().getById(npcData.getNpcId());
        } catch (IllegalArgumentException e) {
            SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error while trying to get NPC with id " + npcData.getNpcId() + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public String name() {
        return "citizens";
    }

    @Override
    public String type() {
        return "integration";
    }

    public boolean isNpc(UUID playerId) {
        return playerId.version() == 2 || CitizensAPI.getNPCRegistry().getByUniqueIdGlobal(playerId) != null;
    }

    public boolean isNpc(Player player) {
        return CitizensAPI.getNPCRegistry().isNPC(player);
    }

    public long getNearbyAuctionTax(Location location, int radius) {
        long tax = Long.MAX_VALUE;
        for (Entity entity : location.getNearbyEntities(radius, radius, radius)) {
            NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
            if (npc != null) {
                SettlementsTrait trait = npc.getTraitNullable(SettlementsTrait.class);
                if (trait != null && trait.getOwner().getAuctionTax() < tax) {
                    tax = trait.getOwner().getAuctionTax();
                }
                AuctionTaxTrait auctionTrait = npc.getTraitNullable(AuctionTaxTrait.class);
                if (trait != null && auctionTrait.getAuctionTax() < tax) {
                    tax = auctionTrait.getAuctionTax();
                }
            }
        }
        if (tax < Long.MAX_VALUE) {
            return tax;
        }
        return -1;
    }

    @TraitName("settlements")
    public static class SettlementsTrait extends Trait implements ConfigMessages {

        public SettlementsTrait() {
            super("settlements");
        }

        @Persist(value = "type", required = true)
        private NpcType type;

        @Persist(value = "ownerType", required = true)
        private String ownerType;

        @Persist(value = "ownerId", required = true)
        private int ownerId;

        @Persist("messages")
        private List<String> messages;

        private Map<UUID, Long> speakCooldown = new HashMap<>();
        private Multimap<UUID, String> spokenMessages = MultimapBuilder.hashKeys().hashSetValues().build();

        public LandOwner<?, ?> getOwner() {
            if ("settlement".equals(ownerType)) {
                return SettlementManager.getInstance().getFromId(IdObject.get(ownerId));
            } else if ("empire".equals(ownerType)) {
                return EmpireManager.getInstance().getEmpireFromId(IdObject.get(ownerId));
            }
            return null;
        }

        public NpcType getType() {
            return type;
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            speakCooldown.remove(event.getPlayer().getUniqueId());
            spokenMessages.removeAll(event.getPlayer().getUniqueId());
        }

        @Override
        public void run() {
            // Only continue if this NPC has spawned
            if (!npc.isSpawned()) {
                return;
            }

            // Don't run if Settlements isn't loaded yet
            if (SettlementsPlugin.getInstance() == null || !SettlementsPlugin.getInstance().isRunning()) {
                return;
            }

            LandOwner landOwner = getOwner();
            if (landOwner == null) {
                npc.despawn();
                CitizensAPI.getNPCRegistry().deregister(npc);
                if (npc.getEntity() != null) {
                    npc.getEntity().remove();
                }
                SettlementsPlugin.logDebug("Despawning NPC " + type + " due to land landowner " + ownerType + " id:" + ownerId + " not being found?");
                return;
            }
            int range = Config.Citizens.TALK_RANGE;
            // Check whether any player are nearby and handle talking to them

            long currentTime = System.currentTimeMillis();

            for (Player player : npc.getEntity().getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(npc.getEntity().getLocation()) < range * range) {
                    long lastSpoken = speakCooldown.getOrDefault(player.getUniqueId(), 0L);
                    // Check if there is a talk delay and if the cooldown has been reached or if no message was sent before
                    if (lastSpoken + Config.Citizens.TALK_DELAY * 1000 < currentTime) {
                        // Get which message should be sent
                        String message = chooseMessage(player);
                        if (message != null && !message.isEmpty()) {
                            // If there should be a message, send it
                            getMessage("talk").tell(player,
                                    "message", message,
                                    "state", landOwner.getStateTag(),
                                    "name", landOwner.getName(),
                                    "player", player.getName(),
                                    "npc", type.getTag()
                            );
                            if (!Config.Citizens.REPEAT_MESSAGES) {
                                spokenMessages.put(player.getUniqueId(), message);
                            }
                            if (Config.Citizens.TALK_DELAY > 0) {
                                // Store cooldown if there is a delay
                                speakCooldown.put(player.getUniqueId(), currentTime);
                            }
                        }
                    }
                } else {
                    // Remove cooldown when player is outside range
                    speakCooldown.remove(player.getUniqueId());
                    if (Config.Citizens.TALK_DELAY > 0) {
                        // Remove spoken message if there is a delay
                        spokenMessages.removeAll(player.getUniqueId());
                    }
                }
            }
        }

        /**
         * Choose a random message from the available ones
         * @param player The player to choose for
         * @return The random message or null if none is available and all were used
         */
        private String chooseMessage(Player player) {
            Collection<String> spoken = spokenMessages.get(player.getUniqueId());
            if (spoken.size() < messages.size()) {
                int startIndex = RANDOM.nextInt(messages.size());
                if (Config.Citizens.REPEAT_MESSAGES) {
                    return messages.get(startIndex);
                }
                int index = startIndex;
                String message;
                // Get the message at the index and check if it was already spoken it
                while (spoken.contains(message = messages.get(index))) {
                    index++;
                    // Loop back to start as the start index is somewhere at random
                    if (index == messages.size()) {
                        index = 0;
                    }
                    // We haven't found anything if we reached the start again
                    if (index == startIndex) {
                        return null;
                    }
                }
                return message;
            }
            return null;
        }

        @Override
        public String name() {
            return "citizens";
        }

        @Override
        public String type() {
            return "integration";
        }
    }

    @TraitName("auctiontax")
    public static class AuctionTaxTrait extends Trait  {

        public AuctionTaxTrait() {
            super("auctiontax");
        }

        @Persist(value = "auctiontax", required = true)
        private long auctionTax;

        public long getAuctionTax() {
            return auctionTax;
        }

        public long setAuctionTax(long auctionTax) {
            return auctionTax;
        }
    }
}
