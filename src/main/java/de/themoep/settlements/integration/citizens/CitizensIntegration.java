package de.themoep.settlements.integration.citizens;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.LandownerLevel;
import de.themoep.settlements.npc.NpcData;
import de.themoep.settlements.npc.NpcType;
import de.themoep.settlements.land.events.LandownerLeveledEvent;
import de.themoep.settlements.land.gui.BankGUI;
import de.themoep.settlements.land.gui.UpgradeGUI;
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
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.Locale;

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

    void createNpc(LandOwner<?, ?> landOwner, NpcType type, Location location) {
        NPC npc = getNpc(landOwner, type);
        if (npc != null) {
            if (npc.isSpawned()) {
                npc.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
            } else {
                npc.spawn(location);
            }
            return;
        }

        String key = landOwner + "." + type.getKey();
        EntityType entityType = getType(key);
        npc = CitizensAPI.getNPCRegistry().createNPC(entityType, getName(key));

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

        List<String> messages = ConfigManager.getInstance().getNpcConfig().getStringList(key + ".messages");
        if (!messages.isEmpty()) {
            Text textTrait = npc.getOrAddTrait(Text.class);

            int messageDelay = ConfigManager.getInstance().getNpcConfig().getInt(key + ".message-delay", 0);
            int messageRange = ConfigManager.getInstance().getNpcConfig().getInt(key + ".message-range", 0);

            for (String message : messages) {
                textTrait.add(MessageUtils.replaceColorCodes(message));
            }

            if (messageDelay > 0) {
                textTrait.setDelay(messageDelay);
            }
            if (messageRange > 0) {
                textTrait.setRange(messageRange);
            }

            if (!textTrait.shouldTalkClose()) {
                textTrait.toggle();
            }
        }

        switch (entityType) {
            case PLAYER: {
                SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);

                String skinName = ConfigManager.getInstance().getNpcConfig().getString(key + ".skin.name", "");
                String texture = ConfigManager.getInstance().getNpcConfig().getString(key + ".skin.texture", "");
                String signature = ConfigManager.getInstance().getNpcConfig().getString(key + ".skin.signature", "");
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
                String villagerType = ConfigManager.getInstance().getNpcConfig().getString(key + ".villager.type", "");
                if (!villagerType.isEmpty()) {
                    VillagerTrait villagerTrait = npc.getOrAddTrait(VillagerTrait.class);
                    try {
                        villagerTrait.setType(Villager.Type.valueOf(villagerType.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException e) {
                        SettlementsPlugin.getInstance().getLogger().warning("Unknown villager type " + villagerType + " configured for " + type);
                    }
                }

                String villagerProfession = ConfigManager.getInstance().getNpcConfig().getString(key + ".villager.profession", "");
                if (!villagerProfession.isEmpty()) {
                    VillagerProfession professionTrait = npc.getOrAddTrait(VillagerProfession.class);
                    try {
                        professionTrait.setProfession(Villager.Profession.valueOf(villagerProfession.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException e) {
                        SettlementsPlugin.getInstance().getLogger().warning("Unknown villager profession " + villagerType + " configured for " + type);
                    }
                }
            }
        }

        NpcData npcData = landOwner.getNpcData(type);
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

    private String getName(String type) {
        return ConfigManager.getInstance().getNpcConfig().getString(type + ".name");
    }

    private EntityType getType(String type) {
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
            LandOwner<?, ?> landOwner;
            if (typeTrait.ownerType.equals("settlement")) {
                landOwner = SettlementManager.getInstance().getFromId(IdObject.get(typeTrait.ownerId));
            } else if (typeTrait.ownerType.equals("empire")) {
                landOwner = EmpireManager.getInstance().getEmpireFromId(IdObject.get(typeTrait.ownerId));
            } else {
                return;
            }

            switch (typeTrait.type) {
                case MANAGER: {
                    if (landOwner.hasPermission(resident, "settings")) {
                        new ManagementGUI(resident).open();
                    }
                    return;
                }
                case QUARTER_MASTER: {
                    if (landOwner.hasPermission(resident, "upgrade")) {
                        new UpgradeGUI(resident, landOwner).open();
                    }
                    return;
                }
                case BANKER: {
                    if (landOwner.hasPermission(resident, "bank")) {
                        new BankGUI(resident, landOwner).open();
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
                    // TODO: Open auction GUI
                    return;
                }
                case BLACKSMITH: {
                    // TODO: Refine GUI
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
        if (npcData == null) {
            return null;
        }
        return CitizensAPI.getNPCRegistry().getById(npcData.getNpcId());
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

        protected SettlementsTrait() {
            super("settlements");
        }

        @Persist(required = true)
        private NpcType type;

        @Persist(required = true)
        private String ownerType;

        @Persist(required = true)
        private int ownerId;
    }
}
