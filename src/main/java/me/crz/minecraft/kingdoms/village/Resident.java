package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Resident implements ConfigurationSerializable, Targetable {
    private Object id = null;
    private UUID uuid;
    private String name;
    private VillageRank rank = VillageRank.getDefault();
    private EmpireRank empireRank = EmpireRank.getDefault();
    private String title = null;
    private Village village = null;
    private ChatChannel activeChannel = ChatChannel.GENERAL;
    private Set<ChatChannel> channels = EnumSet.of(ChatChannel.GENERAL);
    private Set<UUID> ignore = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Set<UUID> friends = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private boolean swear = true;
    private int bonusClaims = 0;
    private long lastLogin = 0;
    private long villageJoin;

    // Temporary, per session settings:
    private boolean resourcePack = false;
    private boolean showSidebar = true;
    private boolean hasEmpireFlight = false;
    private boolean hasMessagesEnabled = true;

    public Resident(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    /**
     * Tests if a resident has a certain permission based on his rank or Bukkit permissions
     * @param permission    Permission to check
     * @return              The value of the permission or false
     */
    public boolean hasPermission(String permission) {
        if (!permission.startsWith("kingdoms.")) {
            permission = "kingdoms.command." + permission;
        }
        if (hasCommandPermission(permission)) {
            Player player = getPlayer();
            if (player != null) {
                return player.hasPermission(permission);
            }
        }
        return false;
    }

    private boolean hasCommandPermission(String permission) {
        if (permission.startsWith("kingdoms.command.")) {
            String rankPerm = permission.substring("kingdoms.command.".length());
            if (!hasVillage() && !VillageRank.getChief().getInheritedPermissionValue(rankPerm).isPresent()) {
                return true;
            }

            Optional<Boolean> villageValue = getVillage().getEffectivePermissionValue(getVillageRank(), rankPerm);
            if (villageValue.isPresent()) {
                return villageValue.get();
            }

            if (rankPerm.startsWith("empire.") && getVillage().hasEmpire()) {
                Optional<Boolean> empireValue = getVillage().getEmpire().getEffectivePermissionValue(getEmpireRank(), rankPerm);
                if (empireValue.isPresent()) {
                    return empireValue.get();
                }
                return false;
            }
            return !VillageRank.getChief().getInheritedPermissionValue(rankPerm).isPresent();
        }
        return true;
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
        KingdomsPlugin.getInstance().getIgnoreProviders().forEach(i -> i.addIgnore(this, ignore));
        return this.ignore.add(ignore);
    }

    public boolean removeIgnore(UUID ignore) {
        KingdomsPlugin.getInstance().getIgnoreProviders().forEach(i -> i.removeIgnore(this, ignore));
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

    public boolean isListening(ChatChannel channel) {
        return getChannels().contains(channel);
    }

    public boolean addChannel(ChatChannel channel) {
        Message.get("command.chat.joined").tell(this, "name", channel.getTag());
        return channels.add(channel);
    }

    public boolean removeChannel(ChatChannel channel) {
        Message.get("command.chat.leave.success").tell(this, "name", channel.getTag());
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
        setVillage(VillageManager.getInstance().getVillageFromName(name));
    }

    public String getVillageName() {
        return village != null ? village.getName() : "";
    }

    public void setVillage(Village village) {
        if (getVillage() != null) {
            getVillage().getResidentIds().remove(getUUID());
            if (hasEmpire()) {
                getVillage().getEmpire().removeSenator(this);
            }
        }
        this.village = village;
        if (village != null) {
            village.getResidentIds().add(getUUID());
            setVillageJoin(System.currentTimeMillis());
        } else {
            setVillageRank(VillageRank.getDefault());
            setEmpireRank(EmpireRank.getDefault());
            setVillageJoin(-1);
            setTitle(null);
        }
    }

    public Village getVillage() {
        return village;
    }

    public boolean leaveVillage() {
        if (hasVillage()) {
            getVillage().removeMember(this);
            return true;
        }
        return false;
    }

    public void setVillageRank(VillageRank rank) {
        if (this.rank == rank) {
            return;
        }
        VillageRank prev = getVillageRank();
        this.rank = rank;
        if (hasEmpire() && getVillage().getEmpire().isCapital(getVillage())) {
            if (rank == VillageRank.getChief()) {
                setEmpireRank(EmpireRank.getLeader());
                getVillage().getEmpire().removeSenator(this);
            } else if (prev == VillageRank.getChief()) {
                setEmpireRank(EmpireRank.getLeader().getInheritedRank());
                getVillage().getEmpire().addSenator(this);
            }
        }
    }

    public VillageRank getVillageRank() {
        return rank;
    }

    public EmpireRank getEmpireRank() {
        return empireRank;
    }

    public void setEmpireRank(EmpireRank rank) {
        empireRank = rank;
    }

    public boolean hasEmpire() {
        return hasVillage() && getVillage().hasEmpire();
    }

    public String getTitle() {
        return (hasVillage()
                ? (title != null
                        ? (getVillage().getTitles(getVillageRank()).contains(title)
                                ? title : getVillageRank().getTag(getVillage()))
                        : getVillageRank().getTag(getVillage()))
                : "")
                + (hasEmpire() ? getVillage().getEmpire().getSymbol() : "");
    }

    public boolean setTitle(String title) {
        if (!hasVillage()) {
            this.title = null;
        } else if (title == null) {
            this.title = getVillageRank().getTag(getVillage());
        } else if (getVillage().getTitles(getVillageRank()).contains(title)) {
            this.title = title;
        } else {
            return false;
        }
        return true;
    }

    public int getBonusClaims() {
        return bonusClaims;
    }

    public void setBonusClaims(int bonusClaims) {
        this.bonusClaims = bonusClaims;
    }

    @Override
    public List<Resident> getOnlineResidents() {
        return getPlayer() == null ? Collections.EMPTY_LIST : Collections.singletonList(this);
    }

    @Override
    public boolean isMember(Resident resident) {
        return equals(resident);
    }

    public long getVillageJoin() {
        return getVillage() != null ? villageJoin : -1;
    }

    public void setVillageJoin(long villageJoin) {
        this.villageJoin = villageJoin;
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

    /**
     * Temporary, per session setting
     */
    public void setResourcePack(boolean resourcePack) {
        this.resourcePack = resourcePack;
    }

    /**
     * Temporary, per session setting
     */
    public boolean hasResourcePack() {
        return resourcePack;
    }

    /**
     * Temporary, per session setting
     */
    public boolean showSidebar() {
        return showSidebar;
    }

    /**
     * Temporary, per session setting
     */
    public void showSidebar(boolean showSidebar) {
        this.showSidebar = showSidebar;
    }

    @Override
    public Map<String, Object> serialize() {
        if (hasVillage() && getVillage().getId().isNull()) {
            throw new IllegalArgumentException("Resident had village but village has no ID?");
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", getId());
        map.put("uuid", getUUID().toString());
        map.put("name", getName());
        map.put("settlementId", getVillage() != null ? getVillage().getId().value() : null);
        map.put("settlementJoin", getVillageJoin());
        map.put("rank", getVillageRank().name());
        map.put("title", title);
        map.put("empirerank", getEmpireRank().name());
        map.put("lastLogin", getLastLogin());
        map.put("channels", getChannels().stream().map(ChatChannel::name).collect(Collectors.toList()));
        map.put("activeChannel", getActiveChannel().name());
        map.put("swear", getSwear());
        map.put("bonusClaims", getBonusClaims());
        map.put("ignores", getIgnore().stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("friends", getFriends().stream().map(UUID::toString).collect(Collectors.toList()));
        return map;
    }

    public static Resident deserialize(Map<String, Object> map) {
        if (map == null || map.get("uuid") == null || map.get("name") == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize Resident! (" + map + ")");
            return null;
        }
        Resident resident = new Resident(UUID.fromString((String) map.get("uuid")), (String) map.get("name"));
        resident.setId(map.get("id"));
        if (resident.getId() == null) {
            resident.setId(map.get("_id"));
        }
        try {
            resident.setVillageRank(VillageRank.valueOf((String) map.get("rank")));
        } catch (IllegalArgumentException e) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Invalid village rank for resident " + resident.getName() + "/" + resident.getUUID() + ": " + map.get("rank"));
        }
        try {
            resident.setEmpireRank(EmpireRank.valueOf((String) map.get("empirerank")));
        } catch (IllegalArgumentException e) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Invalid empire rank for resident " + resident.getName() + "/" + resident.getUUID() + ": " + map.get("empirerank"));
        }
        if (map.get("villageName") != null) {
            resident.setVillage(VillageManager.getInstance().getVillageFromName((String) map.get("villageName")));
        } else {
            resident.setVillage(VillageManager.getInstance().getVillageFromId(IdObject.get(map.get("settlementId"))));
        }
        resident.setTitle((String) map.get("title"));
        resident.setVillageJoin((Long) map.get("settlementJoin"));
        resident.setLastLogin((Long) map.get("lastLogin"));
        resident.getChannels().clear();
        ((Collection<String>) map.get("channels")).stream()
                .map(s -> Utils.parseEnum(ChatChannel.class, s.toUpperCase()))
                .filter(Objects::nonNull)
                .forEach(c -> resident.getChannels().add(c));
        try {
            resident.setActiveChannel(ChatChannel.valueOf(((String) map.get("activeChannel")).toUpperCase()));
        } catch (Exception e) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Invalid active channel for resident " + resident.getName() + "/" + resident.getUUID() + ": " + map.get("activeChannel"));
        }
        if (map.containsKey("swear")) {
            resident.setSwear((Boolean) map.get("swear"));
        }
        if (map.containsKey("bonusClaims")) {
            resident.setBonusClaims((Integer) map.get("bonusClaims"));
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

    public void tell(Message message, String... replacements) {
        MessageUtils.tell(this, message, replacements);
    }

    public void tell(String message, String... replacements) {
        MessageUtils.tell(this, message, replacements);
    }

    public void tell(BaseComponent[] message) {
        MessageUtils.tell(this.getPlayer(), message);
    }

    public boolean hasAllies() {
        return hasVillage() && getVillage().hasAllies() || (hasEmpire() && getVillage().getEmpire().hasAllies());
    }

    public boolean hasEmpireFlight() {
        return hasEmpireFlight;
    }

    public void setHasEmpireFlight(boolean hasEmpireFlight) {
        this.hasEmpireFlight = hasEmpireFlight;
    }

    public boolean hasMessagesEnabled() {
        return hasMessagesEnabled;
    }

    public void setHasMessagesEnabled(boolean hasMessagesEnabled) {
        this.hasMessagesEnabled = hasMessagesEnabled;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode() * 31 + (id == null ? 0 : id.hashCode());
    }
}
