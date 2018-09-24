package me.crz.minecraft.kingdoms.village;

import java.lang.reflect.Field;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import org.bson.Document;
import java.util.List;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import java.util.UUID;

public class Resident
{
    private UUID uuid;
    private VillageRank rank;
    private String villageName;
    private ChatChannel activeChannel;
    private List<String> channels;
    private List<String> ignore;
    private List<String> friends;
    private boolean swear;
    private Document document;
    
    public Resident(final UUID uuid) {
        super();
        this.initUUID(uuid);
    }
    
    public void initUUID(final UUID uuid) {
        document = new Document();
        (channels = new ArrayList<String>()).add(ChatChannel.ALLY.name());
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
        rank = rank;
        villageName = villageName;
    }
    
    public Resident(final Document document, final UUID uuid) {
        super();
        if (document == null && uuid != null) {
            this.initUUID(uuid);
            return;
        }
        document = document;
        uuid = UUID.fromString(document.getString((Object)"uuid"));
        rank = VillageRank.valueOf(document.getString((Object)"rank"));
        villageName = document.getString((Object)"villageName");
        if (document.containsKey((Object)"channels")) {
            channels = (List<String>)document.get((Object)"channels");
        }
        else {
            this.set("channels", new ArrayList());
        }
        if (document.containsKey((Object)"activeChannel")) {
            try {
                activeChannel = ChatChannel.valueOf(document.getString((Object)"activeChannel").toUpperCase());
            }
            catch (Exception e) {
                this.set("activeChannel", ChatChannel.GENERAL);
            }
        }
        else {
            this.set("activeChannel", ChatChannel.GENERAL);
        }
        if (document.containsKey((Object)"swear")) {
            swear = document.getBoolean((Object)"swear");
        }
        else {
            this.set("swear", false);
        }
        if (document.containsKey((Object)"ignore")) {
            ignore = (List<String>)document.get((Object)"ignore");
        }
        else {
            this.set("ignore", new ArrayList());
        }
        if (document.containsKey((Object)"friends")) {
            friends = (List<String>)document.get((Object)"friends");
        }
        else {
            this.set("friends", new ArrayList());
        }
    }
    
    public boolean hasVillage() {
        return !this.getVillageName().isEmpty();
    }
    
    public List<String> getFriends() {
        return friends;
    }
    
    public void addFriend(final String name) {
        friends.add(name);
        this.set("friends", friends, false);
    }
    
    public void removeFriend(final String name) {
        friends.remove(name);
        this.set("friends", friends, false);
    }
    
    public List<String> getIgnore() {
        return ignore;
    }
    
    public void addIgnore(final String ignore) {
        this.ignore.add(ignore);
        this.set("ignore", this.ignore, false);
    }
    
    public void removeIgnore(final String ignore) {
        this.ignore.remove(ignore);
        this.set("ignore", this.ignore, false);
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
                        if ((boolean)b) {
                            field.set(this, o);
                        }
                        document.put(key, (Object)((Enum)o).name());
                    }
                    else if (o instanceof UUID) {
                        if ((boolean)b) {
                            field.set(this, o);
                        }
                        document.put(key, (Object)((UUID)o).toString());
                    }
                    else {
                        if ((boolean)b) {
                            field.set(this, o);
                        }
                        document.put(key, o);
                    }
                }
                catch (IllegalArgumentException | IllegalAccessException ex2) {
                    final Exception ex;
                    final Exception e = ex;
                    e.printStackTrace();
                }
            }
        }
    }
}
