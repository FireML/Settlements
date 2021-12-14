package de.themoep.settlements.integration.citizens;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.events.SettlementCreatedEvent;
import de.themoep.settlements.settlement.gui.ManagementGUI;
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

import java.util.List;
import java.util.Locale;

public class CitizensIntegration implements Listener {


    public CitizensIntegration() {
        SettlementsPlugin.getInstance().getServer().getPluginManager().registerEvents(this, SettlementsPlugin.getInstance());

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SettlementsTrait.class));

    }

    public String getCitizensVersion() {
        return CitizensAPI.getPlugin().getDescription().getVersion();
    }

    @EventHandler
    public void onSettlementCreate(SettlementCreatedEvent event) {
        createNpc(event.getSettlement(), "manager", event.getResident().getPlayer().getLocation());
        createNpc(event.getSettlement(), "quarter_master", event.getResident().getPlayer().getLocation());
    }

    private void createNpc(Settlement landOwner, String type, Location location) {
        String key = landOwner + "." + type;
        EntityType entityType = getType(key);
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(entityType, getName(key));

        npc.setProtected(true);

        SettlementsTrait typeTrait = npc.getOrAddTrait(SettlementsTrait.class);
        typeTrait.type = type;
        typeTrait.ownerType = landOwner.getDbName();
        typeTrait.ownerId = (int) landOwner.getId().value();

        Anchors anchors = npc.getOrAddTrait(Anchors.class);
        anchors.addAnchor("home", location);

        Waypoints waypoints = npc.getOrAddTrait(Waypoints.class);
        waypoints.setWaypointProvider("wander");
        WanderWaypointProvider wander = (WanderWaypointProvider) waypoints.getCurrentProvider();
        wander.setXYRange(2, 2);
        wander.addRegionCentre(location);

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
            LandOwner landOwner;
            if (typeTrait.ownerType.equals("settlement")) {
                landOwner = SettlementManager.getInstance().getFromId(IdObject.get(typeTrait.ownerId));
            } else if (typeTrait.ownerType.equals("empire")) {
                landOwner = EmpireManager.getInstance().getEmpireFromId(IdObject.get(typeTrait.ownerId));
            } else {
                return;
            }
            switch (typeTrait.type) {
                case "manager": {
                    if (landOwner.hasPermission(resident, "settings")) {
                        new ManagementGUI(resident).open();
                    }
                    return;
                }
            }
        }
    }

    @TraitName("settlements")
    public static class SettlementsTrait extends Trait {

        protected SettlementsTrait() {
            super("settlements");
        }

        @Persist(required = true)
        private String type;

        @Persist(required = true)
        private String ownerType;

        @Persist(required = true)
        private int ownerId;
    }
}
