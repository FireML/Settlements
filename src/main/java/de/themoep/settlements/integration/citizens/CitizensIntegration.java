package de.themoep.settlements.integration.citizens;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.empire.gui.WorldGUI;
import de.themoep.settlements.land.*;
import de.themoep.settlements.land.events.*;
import de.themoep.settlements.land.gui.BankGUI;
import de.themoep.settlements.land.gui.ElderGUI;
import de.themoep.settlements.land.gui.UpgradeGUI;
import de.themoep.settlements.npc.NpcConfig;
import de.themoep.settlements.npc.NpcData;
import de.themoep.settlements.npc.NpcManager;
import de.themoep.settlements.npc.NpcType;
import de.themoep.settlements.npc.events.NPCToggleEvent;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.gui.OutpostsGUI;
import de.themoep.settlements.utils.*;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import de.themoep.settlements.world.SimpleLocation;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.CommandConfigurable;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.trait.*;
import net.citizensnpcs.trait.versioned.VillagerTrait;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;
import java.util.logging.Level;

public class CitizensIntegration implements Listener, ConfigMessages, LandAccessChecker {

    private static final Random RANDOM = new Random();

    public CitizensIntegration() {
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SettlementsTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(AuctionTaxTrait.class));

        SubCommand settlementCommand = CommandManager.getInstance().getCommand("settlement");
        settlementCommand.registerCommand(new NpcCommand(this, settlementCommand, Resident::getSettlement));
        SubCommand empireCommand = CommandManager.getInstance().getCommand("empire");
        empireCommand.registerCommand(new NpcCommand(this, empireCommand, r -> r.hasEmpire() ? r.getSettlement().getEmpire() : null));
        SettlementsPlugin.getInstance().getServer().getPluginManager().registerEvents(this, SettlementsPlugin.getInstance());

        if (Config.Citizens.FORCE_RESPAWN_NPCS) {
            for (Settlement settlement : SettlementManager.getInstance().getSettlements()) {
                if (settlement.isEnableNPCs()) {
                    forceSpawn(settlement);
                }
            }
            for (Empire empire : EmpireManager.getInstance().getEmpires()) {
                if (empire.isEnableNPCs()) {
                    forceSpawn(empire);
                }
            }
        }

        for (Settlement settlement : SettlementManager.getInstance().getSettlements()) {
            if (settlement.isEnableNPCs()) {
                updateName(settlement);
            }
        }
        for (Empire empire : EmpireManager.getInstance().getEmpires()) {
            if (empire.isEnableNPCs()) {
                updateName(empire);
            }
        }
    }

    private void forceSpawn(LandOwner<?, ?> landOwner) {
        if (!landOwner.isEnableNPCs()) return;
        for (NpcData npcData : landOwner.getNpcData()) {
            if (npcData.isUnlocked() && npcData.isAvailable()) {
                NPC npc = getNpc(npcData);
                if (npc != null && !npc.isSpawned()) {
                    Location location = npc.getStoredLocation();
                    if (location != null) {
                        OwnedLand land = LandManager.getInstance().getLand(location);
                        if (land != null && land.isOwnedBy(landOwner) && landOwner.isEnableNPCs()) {
                            npc.spawn(location);
                        }
                    }
                }
            }
        }
    }

    private void updateName(LandOwner<?, ?> landOwner) {
        if (!landOwner.isEnableNPCs()) {
            return;
        }
        for (NpcData npcData : landOwner.getNpcData()) {
            NPC npc = getNpc(npcData);
            if (npc != null && !npc.getName().equals(ChatColor.stripColor(npcData.getConfig().getName()))) {
                npc.setName(npcData.getConfig().getName());
            }
        }
    }

    public String getCitizensVersion() {
        return CitizensAPI.getPlugin().getDescription().getVersion();
    }

    @EventHandler
    public void onCreate(LandownerCreatedEvent event) {
        checkForNew(event.getResident(), event.getLandOwner());
    }

    public void checkForNew(Resident resident, LandOwner<?, ?> landOwner) {
        if (!landOwner.isEnableNPCs()) {
            return;
        }
        for (NpcType npc : landOwner.getLevelInfo().getInheritedNPCs()) {
            NpcData npcData = landOwner.getNpcData(npc);
            if (!npcData.isUnlocked()
                    && unlockNpc(resident, landOwner, npc)
                    && landOwner.getHome() != null) {
                NpcConfig config = NpcManager.getInstance().getConfig(landOwner, npc);
                createNpc(landOwner, npc, config, landOwner.getHome().toBukkitLocation());
                getMessage("new-npc-available").tell(landOwner,
                        "npc", config.getName(),
                        "type", npc.getKey(),
                        "state", landOwner.getStateTag());
            }
        }
    }

    @EventHandler
    public void onClaimed(LandClaimEvent event) {
        if (!event.getLandOwner().isEnableNPCs()) {
            return;
        }
        checkForNew(event.getLandOwner().getChief(), event.getLandOwner());
        for (NpcData data : event.getLandOwner().getNpcData()) {
            handle(event, data.getType());
        }
        for (NpcType npc : event.getLandOwner().getLevelInfo().getInheritedNPCs()) {
            handle(event, npc);
        }
    }

    private void handle(LandClaimEvent event, NpcType type) {
        if (!event.getLandOwner().isEnableNPCs()) {
            return;
        }
        NpcData npcData = event.getLandOwner().getNpcData(type);
        if (npcData.isUnlocked() && npcData.isAvailable()) {
            NPC npc = getNpc(npcData);
            if (npc == null) {
                Location location;
                if (event.getLandOwner().getHome() != null) {
                    location = event.getLandOwner().getHome().toBukkitLocation();
                } else {
                    location = event.getLand().getCenterBukkitLocation();
                }
                npc = createNpc(event.getLandOwner(), type, location);
                getMessage("new-npc-available").tell(event.getLandOwner(),
                        "npc", npcData.getTag(),
                        "type", npcData.getTag().replace(' ', '_'),
                        "state", event.getLandOwner().getStateTag());
                if (npcData.getNpcId() != npc.getId()) {
                    npcData.setNpcId(npc.getId());
                    npcData.save();
                }
            }
        }
    }

    public NPC createNpc(LandOwner<?, ?> landOwner, NpcType type, Location location) {
        if (landOwner.isEnableNPCs()) {
            return createNpc(landOwner, type, NpcManager.getInstance().getConfig(landOwner, type), location);
        }
        return null;
    }

    NPC createNpc(LandOwner<?, ?> landOwner, NpcType type, NpcConfig config, Location location) {
        if (!landOwner.isEnableNPCs()) {
            return null;
        }

        if (config == null) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Tried to create " + type + " NPC for " + landOwner.getKey() + " " + landOwner.getName() + " but there was no config specified for that NPC type?");
            return null;
        }

        NpcData npcData = landOwner.getNpcData(type);
        NPC npc = getNpc(npcData);
        if (npc != null) {
            npc.getOrAddTrait(CurrentLocation.class).setLocation(location);
            if (npc.isSpawned()) {
                npc.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
            } else {
                npc.spawn(location);
            }
            return npc;
        }

        OwnedLand land = LandManager.getInstance().getLand(location);
        if (land != null && !land.isOwnedBy(landOwner)) {
            return null;
        }

        EntityType entityType = npcData.getNpcSkinName().isEmpty() ? config.getEntityType() : EntityType.PLAYER;
        npc = CitizensAPI.getNPCRegistry().createNPC(entityType, config.getName());

        npc.setProtected(true);

        npc.getOrAddTrait(CurrentLocation.class).setLocation(location);

        npc.getOrAddTrait(Spawned.class).setSpawned(true);

        SettlementsTrait typeTrait = npc.getOrAddTrait(SettlementsTrait.class);
        typeTrait.type = type;
        typeTrait.ownerType = landOwner.getDbName();
        typeTrait.ownerId = (int) landOwner.getId().value();
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
            npcData.checkLevelUp(null);
        }
        landOwner.addNpc(npcData);
        npcData.save();
        npc.spawn(location);
        //npc.save(); // TODO: Do we need to save them?
        return npc;
    }

    void applyNpcTraits(NPC npc, NpcConfig config) {
        switch (config.getEntityType()) {
            case PLAYER -> {
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
            }
            case VILLAGER -> {
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

            Pair<Boolean, String> access = canUse(event.getClicker(), Use.NPC, event.getNPC().getEntity().getLocation());
            if (!access.getLeft()) {
                MessageUtils.tell(event.getClicker(), access.getRight());
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
                            new de.themoep.settlements.settlement.gui.ManagementGUI(resident).open();
                        } else if (landOwner instanceof Empire) {
                            new de.themoep.settlements.empire.gui.ManagementGUI(resident).open();
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
                    Bukkit.dispatchCommand(resident.getPlayer(), "ah");
                    return;
                }
                case BLACKSMITH: {
                    // Handled in refine plugin
                }
            }
        }
    }

    @EventHandler
    public void onLeveled(LandownerLeveledEvent event) {
        LandownerLevel level = event.getLeveled().getLevelInfo();
        if (event.getLeveled().isEnableNPCs()) {
            return;
        }
        if (level != null) {
            for (NpcType npc : level.getNPCs()) {
                if (unlockNpc(event.getResident(), event.getLeveled(), npc)) {
                    if (event.getLeveled().getHome() != null) {
                        createNpc(event.getLeveled(), npc, event.getLeveled().getHome().toBukkitLocation());
                        getMessage("new-npc-available").tell(event.getLeveled(),
                                "npc", npc.getTag(),
                                "type", npc.getTag().replace(' ', '_'),
                                "state", event.getLeveled().getStateTag());
                    } else {
                        getMessage("new-npc-unlocked").tell(event.getLeveled(),
                                "npc", npc.getTag(),
                                "type", npc.getTag().replace(' ', '_'),
                                "state", event.getLeveled().getStateTag());
                    }
                }
            }
        }
    }

    private boolean unlockNpc(Resident resident, LandOwner<?, ?> landOwner, NpcType type) {
        if (landOwner.isEnableNPCs() && NpcManager.getInstance().getConfig(landOwner, type) != null) {
            NpcData npcData = landOwner.getNpcData(type);
            if (!npcData.isUnlocked()) {
                npcData.checkLevelUp(resident);
                Utils.runAsync(() -> DatabaseManager.getInstance().saveNpc(npcData));
            }
            landOwner.addNpc(npcData);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onAbandon(LandownerAbandonedEvent event) {
        removeAll(event.getLandOwner(), "removal of");
    }

    @EventHandler
    public void onDisband(LandownerDisbandEvent event) {
        removeAll(event.getLandOwner(), "removal of");
    }

    @EventHandler
    public void onNPCToggle(NPCToggleEvent event) {
        if (event.isCancelled()) return;
        validateNPCSpawning(event.getLandOwner(), event.getNewState(), true);
    }

    @EventHandler
    public void onCitizensLoad(CitizensEnableEvent event) {
        for (Settlement settlement : SettlementManager.getInstance().getSettlements()) {
            validateNPCSpawning(settlement, settlement.isEnableNPCs(), false);
        }
        for (Empire empire : EmpireManager.getInstance().getEmpires()) {
            validateNPCSpawning(empire, empire.isEnableNPCs(), false);
        }
    }

    public void validateNPCSpawning(LandOwner<?, ?> landOwner, boolean newState, boolean modifyData) {
        checkForNew(landOwner.getChief(), landOwner);
        if (newState) {
            for (NpcData npcData : landOwner.getNpcData()) {
                if (modifyData) npcData.toggle();
                NPC npc = CitizensAPI.getNPCRegistry().getById(npcData.getNpcId());
                if (npc != null) {
                    npc.spawn(landOwner.getHome().toBukkitLocation());
                }
            }
        } else {
            for (NpcData npcData : landOwner.getNpcData()) {
                npcData.toggle();
                int id;
                if ((id = npcData.getNpcId()) < 0) return;
                NPC npc = CitizensAPI.getNPCRegistry().getById(id);
                if (npc != null) {
                    npc.despawn();
                }
            }
        }
    }

    private void removeAll(LandOwner<?, ?> landOwner, String reason) {
        for (NpcData npcData : landOwner.getNpcData()) {
            if (npcData.getNpcId() > -1) {
                NPC npc = CitizensAPI.getNPCRegistry().getById(npcData.getNpcId());
                if (npc != null) {
                    SettlementsPlugin.logDebug("Despawning NPC " + npcData.getType() + " due to " + reason + landOwner.getStateTag() + " " + landOwner.getName());
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
                    Location location = event.getLand().getLandOwner().getHome() != null ? event.getLand().getLandOwner().getHome().toBukkitLocation() : null;
                    if (location != null) {
                        npc.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
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

    public NPC getNpc(NpcData npcData) {
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

            NpcData npcData = landOwner.getNpcData(type);

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
                                    "npc", npcData.getTag()
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
            NpcConfig config = NpcManager.getInstance().getConfig(ownerType, type);
            if (spoken.size() < config.getMessages().size()) {
                int startIndex = RANDOM.nextInt(config.getMessages().size());
                if (Config.Citizens.REPEAT_MESSAGES) {
                    return config.getMessages().get(startIndex);
                }
                int index = startIndex;
                String message;
                // Get the message at the index and check if it was already spoken it
                while (spoken.contains(message = config.getMessages().get(index))) {
                    index++;
                    // Loop back to start as the start index is somewhere at random
                    if (index == config.getMessages().size()) {
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
    public static class AuctionTaxTrait extends Trait implements CommandConfigurable {

        public AuctionTaxTrait() {
            super("auctiontax");
        }

        @Persist(value = "amount", required = true)
        private double amount;

        public double getAuctionTax() {
            return amount;
        }

        public void setAuctionTax(double amount) {
            this.amount = amount;
        }

        @Override
        public void configure(CommandContext args) throws CommandException {
            if (args.length() > 1 && "amount".equalsIgnoreCase(args.getString(1))) {
                double input = args.getDouble(2, -1);
                if (input > -1) {
                    amount = input / 100d;
                    // Citizens can't send messages to the sender ?!?
                    throw new CommandException("Set auction tax amount to " + Utils.toPercentage(amount) + "%");
                } else {
                    throw new CommandException("Please specify a positive auction tax amount or 0 for no tax!");
                }
            }
        }
    }
}
