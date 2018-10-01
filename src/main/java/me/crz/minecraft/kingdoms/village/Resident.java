package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.rank.VillageRank;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Resident implements ConfigurationSerializable {
    private Object id = null;
    private UUID uuid;
    private String name;
    private VillageRank rank = VillageRank.RESIDENT;
    private Village village = null;
    private ChatChannel activeChannel = ChatChannel.GENERAL;
    private Set<ChatChannel> channels = new HashSet<>(Arrays.asList(
            ChatChannel.GENERAL,
            ChatChannel.ALLY,
            ChatChannel.OFFICER,
            ChatChannel.LOCAL,
            ChatChannel.SETTLEMENT
    ));
    private Set<UUID> ignore = new HashSet<>();
    private Set<UUID> friends = new HashSet<>();
    private boolean swear = true;
    private long lastLogin = 0;

    public Resident(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public boolean hasVillage() {
        return village != null;
    }

    public Set<UUID> getFriends() {
        return friends;
    }

    public boolean addFriend(UUID id) {
        return friends.add(id);
    }

    public boolean removeFriend(UUID id) {
        return friends.remove(id);
    }

    public Set<UUID> getIgnore() {
        return ignore;
    }

    public boolean addIgnore(UUID ignore) {
        return this.ignore.add(ignore);
    }

    public boolean removeIgnore(UUID ignore) {
        return this.ignore.remove(ignore);
    }

    public boolean getSwear() {
        return swear;
    }

    public void setSwear(boolean swear) {
        this.swear = swear;
    }

    public ChatChannel getActiveChannel() {
        return activeChannel;
    }

    public boolean setActiveChannel(ChatChannel channel) {
        if (activeChannel != channel) {
            activeChannel = channel;
            return true;
        }
        return false;
    }

    public Set<ChatChannel> getChannels() {
        return channels;
    }

    public boolean addChannel(ChatChannel channel) {
        return channels.add(channel);
    }

    public boolean removeChannel(ChatChannel channel) {
        return channels.remove(channel);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @deprecated Use {@link this#setVillage(Village)}
     */
    @Deprecated
    public void setVillageName(String name) {
        village = VillageManager.getInstance().getVillageFromName(name);
    }

    public String getVillageName() {
        return village != null ? village.getName() : "";
    }

    public void setVillage(Village village) {
        this.village = village;
    }

    public Village getVillage() {
        return village;
    }

    public void setVillageRank(VillageRank rank) {
        this.rank = rank;
    }

    public VillageRank getVillageRank() {
        return rank;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void updateLastLogin() {
        lastLogin = System.currentTimeMillis();
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", getUUID().toString());
        map.put("name", getName());
        map.put("settlementId", village != null ? village.getId().value() : null);
        map.put("rank", getVillageRank().name());
        map.put("lastLogin", getLastLogin());
        map.put("channels", getChannels().stream().map(ChatChannel::name).collect(Collectors.toList()));
        map.put("activeChannel", getActiveChannel().name());
        map.put("swear", getSwear());
        map.put("ignores", getIgnore().stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("friends", getFriends().stream().map(UUID::toString).collect(Collectors.toList()));
        return map;
    }

    public static Resident deserialize(Map<String, Object> map) {
        if (map == null || map.get("uuid") == null || map.get("name") == null) {
            return null;
        }
        Resident resident = new Resident(UUID.fromString((String) map.get("uuid")), (String) map.get("name"));
        resident.setId(map.get("id"));
        if (resident.getId() == null) {
            resident.setId(map.get("_id"));
        }
        resident.setVillageRank(VillageRank.valueOf((String) map.get("rank")));
        if (map.get("villageName") != null) {
            resident.setVillage(VillageManager.getInstance().getVillageFromName((String) map.get("villageName")));
        } else {
            resident.setVillage(VillageManager.getInstance().getVillageFromId(IdObject.get(map.get("settlementId"))));
        }
        resident.setLastLogin((Long) map.get("lastLogin"));
        resident.getChannels().clear();
        ((Collection<String>) map.get("channels")).stream()
                .map(s -> Utils.parseEnum(ChatChannel.class, s.toUpperCase()))
                .filter(Objects::nonNull)
                .forEach(resident::addChannel);
        try {
            resident.setActiveChannel(ChatChannel.valueOf(((String) map.get("activeChannel")).toUpperCase()));
        } catch (Exception e) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Invalid active channel for resident " + resident.getName() + "/" + resident.getUUID() + ": " + map.get("activeChannel"));
        }
        if (map.containsKey("swear")) {
            resident.setSwear((Boolean) map.get("swear"));
        }
        if (map.containsKey("ignores")) {
            ((Collection<String>) map.get("ignores")).stream().map(Utils::parseUuid).filter(Objects::nonNull).forEach(resident::addIgnore);
        }
        if (map.containsKey("friends")) {
            ((Collection<String>) map.get("friends")).stream().map(Utils::parseUuid).filter(Objects::nonNull).forEach(resident::addFriend);
        }
        return resident;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }
}
