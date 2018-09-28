package me.crz.minecraft.kingdoms.village;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import org.bson.Document;

import java.util.HashSet;

import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.rank.VillageRank;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Resident {
    private UUID uuid;
    private String name;
    private VillageRank rank;
    private String villageName;
    private ChatChannel activeChannel;
    private Set<ChatChannel> channels;
    private Set<UUID> ignore;
    private Set<UUID> friends;
    private boolean swear;
    private long lastLogin;
    private Document document;

    public Resident(UUID uuid, String name) {
        super();
        document = new Document();
        channels = new HashSet<>();
        channels.add(ChatChannel.GENERAL);
        channels.add(ChatChannel.ALLY);
        channels.add(ChatChannel.OFFICER);
        channels.add(ChatChannel.LOCAL);
        channels.add(ChatChannel.SETTLEMENT);
        this.set("uuid", uuid);
        this.set("name", name);
        this.set("rank", VillageRank.RESIDENT);
        this.set("villageName", "");
        this.set("channels", channels);
        this.set("activeChannel", ChatChannel.GENERAL);
        this.set("swear", true);
        this.set("ignore", new HashSet<UUID>());
        this.set("friends", new HashSet<UUID>());
        this.set("lastLogin", 0);
    }

    public Resident(Document document) {
        super();
        this.document = document;
        this.uuid = UUID.fromString(document.getString("uuid"));
        this.name = document.getString("name");
        rank = VillageRank.valueOf(document.getString("rank"));
        villageName = document.getString("villageName");
        if (document.containsKey("channels")) {
            channels = document.get("channels", new ArrayList<String>()).stream().map(s -> {
                try {
                    return ChatChannel.valueOf(s.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet());
        } else {
            this.set("channels", new ArrayList());
        }
        if (document.containsKey("activeChannel")) {
            try {
                activeChannel = ChatChannel.valueOf(document.getString("activeChannel").toUpperCase());
            } catch (Exception e) {
                this.set("activeChannel", ChatChannel.GENERAL);
            }
        } else {
            this.set("activeChannel", ChatChannel.GENERAL);
        }
        if (document.containsKey("swear")) {
            swear = document.getBoolean("swear");
        } else {
            this.set("swear", false);
        }
        if (document.containsKey("ignore")) {
            ignore = document.get("ignore", new ArrayList<String>()).stream().map(UUID::fromString).collect(Collectors.toSet());
        } else {
            this.set("ignore", new HashSet<UUID>());
        }
        if (document.containsKey("friends")) {
            friends = document.get("friends", new ArrayList<String>()).stream().map(UUID::fromString).collect(Collectors.toSet());
        } else {
            this.set("friends", new HashSet<UUID>());
        }
        lastLogin = document.getLong("lastLogin");
    }

    public boolean hasVillage() {
        return !this.getVillageName().isEmpty();
    }

    public Set<UUID> getFriends() {
        return friends;
    }

    public boolean addFriend(UUID id) {
        if (friends.add(id)) {
            this.set("friends", friends, false);
            return true;
        }
        return false;
    }

    public boolean removeFriend(UUID id) {
        if (friends.remove(id)) {
            this.set("friends", friends, false);
            return true;
        }
        return false;
    }

    public Set<UUID> getIgnore() {
        return ignore;
    }

    public boolean addIgnore(UUID ignore) {
        if (this.ignore.add(ignore)) {
            this.set("ignore", this.ignore, false);
            return true;
        }
        return false;
    }

    public boolean removeIgnore(UUID ignore) {
        if (this.ignore.remove(ignore)) {
            this.set("ignore", this.ignore, false);
            return true;
        }
        return false;
    }

    public boolean getSwear() {
        return swear;
    }

    public void setSwear(boolean swear) {
        this.set("swear", swear);
    }

    public ChatChannel getActiveChannel() {
        return activeChannel;
    }

    public boolean setActiveChannel(ChatChannel channel) {
        if (activeChannel != channel) {
            this.set("activeChannel", channel);
            return true;
        }
        return false;
    }

    public Set<ChatChannel> getChannels() {
        return channels;
    }

    public boolean addChannel(ChatChannel channel) {
        if (channels.add(channel)) {
            this.set("channels", channels);
            return true;
        }
        return false;
    }

    public boolean removeChannel(ChatChannel channel) {
        if (channels.remove(channel)) {
            this.set("channels", channels);
            return true;
        }
        return false;
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

    public void setVillageName(String name) {
        this.set("villageName", name);
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageRank(VillageRank rank) {
        this.set("rank", rank);
    }

    public VillageRank getVillageRank() {
        return rank;
    }

    public Document getDocument() {
        return document;
    }

    public void set(String key, Object o) {
        this.set(key, o, true);
    }

    public void set(String key, Object o, Boolean setField) {
        Field[] declaredFields;
        for (int length = (declaredFields = this.getClass().getDeclaredFields()).length, i = 0; i < length; ++i) {
            Field field = declaredFields[i];
            if (key.equalsIgnoreCase(field.getName())) {
                try {
                    if (o instanceof Enum) {
                        if (setField) {
                            field.set(this, o);
                        }
                        document.put(key, ((Enum) o).name());
                    } else if (o instanceof UUID) {
                        if (setField) {
                            field.set(this, o);
                        }
                        document.put(key, ((UUID) o).toString());
                    } else if (o instanceof Set) {
                        if (setField) {
                            field.set(this, o);
                        }
                        document.put(key, ((Set<?>) o).stream().map(String::valueOf).collect(Collectors.toList()));
                    } else {
                        if (setField) {
                            field.set(this, o);
                        }
                        document.put(key, o);
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void updateLastLogin() {
        set("lastLogin", System.currentTimeMillis());
    }
}
