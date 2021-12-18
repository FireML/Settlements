package de.themoep.settlements.integration.citizens;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.LandownerLevel;
import de.themoep.settlements.land.events.LandownerLeveledEvent;
import de.themoep.settlements.land.gui.BankGUI;
import de.themoep.settlements.land.gui.UpgradeGUI;
import de.themoep.settlements.npc.NpcConfig;
import de.themoep.settlements.npc.NpcData;
import de.themoep.settlements.npc.NpcManager;
import de.themoep.settlements.npc.NpcType;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.events.SettlementCreatedEvent;
import de.themoep.settlements.settlement.gui.ManagementGUI;
import de.themoep.settlements.teleporter.TeleportersGUI;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.config.ConfigManager;
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
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.trait.versioned.VillagerTrait;
import net.citizensnpcs.trait.waypoint.WanderWaypointProvider;
import net.citizensnpcs.trait.waypoint.Waypoints;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Locale;
import java.util.logging.Level;

public class CitizensIntegration implements Listener, ConfigMessages {


    public CitizensIntegration() {
        SettlementsPlugin.getInstance().getServer().getPluginManager().registerEvents(this, SettlementsPlugin.getInstance());

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SettlementsTrait.class));

        SubCommand settlementCommand = CommandManager.getInstance().getCommand("settlement");
        settlementCommand.registerCommand(new NpcCommand(this, settlementCommand, Resident::getSettlement));
    }

    public String getCitizensVersion() {
        return CitizensAPI.getPlugin().getDescription().getVersion();
    }

    @EventHandler
    public void onSettlementCreate(SettlementCreatedEvent event) {
        createNpc(event.getSettlement(), NpcType.MANAGER, event.getResident().getPlayer().getLocation());
        createNpc(event.getSettlement(), NpcType.QUARTER_MASTER, event.getResident().getPlayer().getLocation());
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

        Anchors anchors = npc.getOrAddTrait(Anchors.class);
        anchors.addAnchor("home", location);

        Waypoints waypoints = npc.getOrAddTrait(Waypoints.class);
        waypoints.setWaypointProvider("wander");
        if (waypoints.getCurrentProvider() instanceof WanderWaypointProvider) {
            WanderWaypointProvider wander = (WanderWaypointProvider) waypoints.getCurrentProvider();
            wander.setXYRange(2, 2);
            wander.addRegionCentre(location);
        }

        if (!config.getMessages().isEmpty()) {
            Text textTrait = npc.getOrAddTrait(Text.class);

            for (String message : config.getMessages()) {
                textTrait.add(MessageUtils.replaceColorCodes(message));
            }

            if (config.getMessageDelay() > 0) {
                textTrait.setDelay(config.getMessageDelay());
            }
            if (config.getMessageRange() > 0) {
                textTrait.setRange(config.getMessageRange());
            }

            if (!textTrait.shouldTalkClose()) {
                textTrait.toggle();
            }
        }

        if (npcData.getNpcSkinName().isEmpty()) {
            applyNpcTraits(npc, config);
        } else {
            npc.getOrAddTrait(SkinTrait.class)
                    .setSkinName(npcData.getNpcSkinName(), true);
        }

        if (npcData.isUnlocked()) {
            npcData.setNpcId(npc.getId());
        } else {
            npcData = new NpcData(npc.getId(), type);
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
                if (!texture.isEmpty() && !signature.isEmpty() && !skinName.isEmpty()) {
                    skinTrait.setSkinPersistent(skinName, signature, texture);
                } else if (!texture.isEmpty() && !signature.isEmpty()) {
                    skinTrait.setTexture(texture, signature);
                } else if (!skinName.isEmpty()) {
                    skinTrait.setSkinName(skinName);
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
                        new ManagementGUI(resident).open();
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
                    if (landOwner.hasPermission(resident, "bank")) {
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
                    // TODO: Outpost-dedicated GUI
                    new TeleportersGUI().open(event.getClicker());
                    return;
                }
                case ELDER: {
                    // TODO: Application process
                    //new ApplicationsGUI(resident, landOwner).open();
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
                createNpc(event.getLeveled(), npc, event.getLeveled().getHome().toBukkitLocation());
                getMessage("new-npcs-available").tell(event.getLeveled(),
                        "npc", npc.getTag(),
                        "state", event.getLeveled().getStateTag());
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

    @TraitName("settlements")
    public static class SettlementsTrait extends Trait {

        public SettlementsTrait() {
            super("settlements");
        }

        @Persist(required = true)
        private NpcType type;

        @Persist(required = true)
        private String ownerType;

        @Persist(required = true)
        private int ownerId;

        public NpcType getType() {
            return type;
        }

        public LandOwner<?, ?> getOwner() {
            if (ownerType.equals("settlement")) {
                return SettlementManager.getInstance().getFromId(IdObject.get(ownerId));
            } else if (ownerType.equals("empire")) {
                return EmpireManager.getInstance().getEmpireFromId(IdObject.get(ownerId));
            }
            return null;
        }
    }
}
