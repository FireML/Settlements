package me.crz.minecraft.kingdoms.village;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;

import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.rank.VillageRank;

import java.util.Set;
import java.util.UUID;

public class Resident {
    private UUID uuid;
    private String name;
    private VillageRank rank = VillageRank.RESIDENT;
    private String villageName = "";
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
        return !this.getVillageName().isEmpty();
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

    public void setVillageName(String name) {
        villageName = name;
    }

    public String getVillageName() {
        return villageName;
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
}
