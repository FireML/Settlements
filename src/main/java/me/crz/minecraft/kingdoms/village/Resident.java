package me.crz.minecraft.kingdoms.village;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import org.bson.Document;

import java.util.LinkedHashSet;
import java.util.List;

import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.rank.VillageRank;

import java.util.Set;
import java.util.UUID;

public class Resident {
    private UUID uuid;
    private VillageRank rank;
    private String villageName;
    private ChatChannel activeChannel;
    private List<String> channels;
    private Set<String> ignore;
    private Set<String> friends;
    private boolean swear;
    private Document document;

    public Resident(final UUID uuid) {
        super();
        this.initUUID(uuid);
    }

    public void initUUID(final UUID uuid) {
        document = new Document();
        (channels = new ArrayList<>()).add(ChatChannel.ALLY.name());
        channels.add(ChatChannel.GENERAL.name());
        channels.add(ChatChannel.OFFICER.name());
        channels.add(ChatChannel.LOCAL.name());
        channels.add(ChatChannel.SETTLEMENT.name());
        this.set("uuid", uuid);
        this.set("rank", VillageRank.RESIDENT);
        this.set("villageName", "");
        this.set("channels", channels);
        this.set("activeChannel", ChatChannel.GENERAL);
        this.set("swear", true);
        this.set("ignore", new ArrayList());
        this.set("friends", new ArrayList());
    }

    public Resident(final UUID uuid, final VillageRank rank, final String villageName) {
        super();
        this.initUUID(uuid);
        this.rank = rank;
        this.villageName = villageName;
    }

    public Resident(final Document document, final UUID uuid) {
        super();
        if (document == null && uuid != null) {
            this.initUUID(uuid);
            return;
        }
        this.document = document;
        this.uuid = UUID.fromString(document.getString("uuid"));
        rank = VillageRank.valueOf(document.getString("rank"));
        villageName = document.getString("villageName");
        if (document.containsKey("channels")) {
            channels = document.get("channels", new ArrayList<>());
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
            ignore = new LinkedHashSet<>((List<String>) document.get("ignore"));
        } else {
            this.set("ignore", new ArrayList());
        }
        if (document.containsKey("friends")) {
            friends = new LinkedHashSet<>((List<String>) document.get("friends"));
        } else {
            this.set("friends", new ArrayList());
        }
    }

    public boolean hasVillage() {
        return !this.getVillageName().isEmpty();
    }

    public Set<String> getFriends() {
        return friends;
    }

    public void addFriend(final String name) {
        friends.add(name);
        this.set("friends", new ArrayList<>(friends), false);
    }

    public void removeFriend(final String name) {
        friends.remove(name);
        this.set("friends", new ArrayList<>(friends), false);
    }

    public Set<String> getIgnore() {
        return ignore;
    }

    public void addIgnore(final String ignore) {
        this.ignore.add(ignore);
        this.set("ignore", new ArrayList<>(this.ignore), false);
    }

    public void removeIgnore(final String ignore) {
        this.ignore.remove(ignore);
        this.set("ignore", new ArrayList<>(this.ignore), false);
    }

    public boolean getSwear() {
        return swear;
    }

    public void setSwear(final boolean swear) {
        this.set("swear", swear);
    }

    public ChatChannel getActiveChannel() {
        return activeChannel;
    }

    public void setActiveChannel(final ChatChannel channel) {
        this.set("activeChannel", channel);
    }

    public List<String> getChannels() {
        return channels;
    }

    public void addChannel(final String channel) {
        if (!channels.contains(channel)) {
            channels.add(channel);
            this.set("channels", channels);
        }
    }

    public void removeChannel(final String channel) {
        if (channels.contains(channel)) {
            channels.remove(channel);
            this.set("channels", channels);
        }
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setVillageName(final String name) {
        this.set("villageName", name);
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageRank(final VillageRank rank) {
        this.set("rank", rank);
    }

    public VillageRank getVillageRank() {
        return rank;
    }

    public Document getDocument() {
        return document;
    }

    public void set(final String key, final Object o) {
        this.set(key, o, true);
    }

    public void set(final String key, final Object o, final Boolean b) {
        Field[] declaredFields;
        for (int length = (declaredFields = this.getClass().getDeclaredFields()).length, i = 0; i < length; ++i) {
            final Field field = declaredFields[i];
            if (key.equalsIgnoreCase(field.getName())) {
                try {
                    if (o instanceof Enum) {
                        if (b) {
                            field.set(this, o);
                        }
                        document.put(key, ((Enum) o).name());
                    } else if (o instanceof UUID) {
                        if (b) {
                            field.set(this, o);
                        }
                        document.put(key, ((UUID) o).toString());
                    } else {
                        if (b) {
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
}
