package de.themoep.settlements.npc;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class NpcConfig {
    private final String name;
    private final EntityType entityType;

    private final Villager.Type villagerType;
    private final Villager.Profession villagerProfession;

    private final String skinName;
    private final UUID skinUuid;
    private final String skinTextures;
    private final String skinSignature;

    private final List<String> messages;
    private final int messageDelay;
    private final int messageRange;

    public NpcConfig(ConfigurationSection config) {
        name = config.getString("name");
        entityType = EntityType.valueOf(config.getString("type", "villager").toUpperCase(Locale.ROOT));

        villagerType = Villager.Type.valueOf(config.getString("villager.type", "plains").toUpperCase(Locale.ROOT));
        villagerProfession = Villager.Profession.valueOf(config.getString("villager.profession", "nitwit").toUpperCase(Locale.ROOT));

        skinName = config.getString("skin.name", "The_Moep");
        String stringId = config.getString("skin.uuid");
        if (stringId != null && stringId.length() == 36) {
            skinUuid = UUID.fromString(stringId);
        }else if (!skinName.isEmpty()) {
            skinUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + skinName).getBytes());
        } else {
            skinUuid = UUID.randomUUID();
        }
        skinTextures = config.getString("skin.textures");
        skinSignature = config.getString("skin.signature");

        messages = config.getStringList("messages");
        messageDelay = config.getInt("message-delay", 0);
        messageRange = config.getInt("message-range", 0);
    }

    public NpcConfig(NpcType npcType) {
        name = npcType.getTag();
        entityType = EntityType.VILLAGER;

        villagerType = Villager.Type.PLAINS;
        villagerProfession = Villager.Profession.NITWIT;

        skinName = "The_Moep";
        skinUuid = UUID.randomUUID();
        skinTextures = "";
        skinSignature = "";

        messages = Collections.emptyList();
        messageDelay = 0;
        messageRange = 0;
    }

    public String getName() {
        return name;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Villager.Type getVillagerType() {
        return villagerType;
    }

    public Villager.Profession getVillagerProfession() {
        return villagerProfession;
    }

    public String getSkinName() {
        return skinName;
    }

    public UUID getSkinUuid() {
        return skinUuid;
    }

    public String getSkinTextures() {
        return skinTextures;
    }

    public String getSkinSignature() {
        return skinSignature;
    }

    public List<String> getMessages() {
        return messages;
    }

    public int getMessageDelay() {
        return messageDelay;
    }

    public int getMessageRange() {
        return messageRange;
    }
}
