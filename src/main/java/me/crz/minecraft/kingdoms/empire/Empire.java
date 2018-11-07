package me.crz.minecraft.kingdoms.empire;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.perk.Perkable;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static me.crz.minecraft.kingdoms.utils.MessageUtils.yesOrNo;

public class Empire implements ConfigurationSerializable, Comparable<Empire>, LandOwner, Perkable<Perk> {
    private IdObject id = IdObject.get(null);
    private String name;
    private IdObject capital;
    private String description = "";
    private String symbol = "";
    private long bank = 0;
    private long joinFee = 0;
    private long tax = 0;
    private boolean pvp = false;
    private boolean fireSpread = false;
    private boolean explosions = false;
    private boolean mobSpawning = false;
    private boolean buff = true;
    private Set<IdObject> members = new LinkedHashSet<>();
    private Set<UUID> senators = new HashSet<>();
    private int bonusClaims = 0;
    private Map<String, Long> vault = new HashMap<>();
    private Map<String, PerkData> perks = new LinkedHashMap<>();
    private SimpleLocation home;
    private BuildState buildState = BuildState.PRIVATE;
    private Map<Use, BuildState> use = new EnumMap<>(Use.class);

    private Set<UUID> banishedPlayers = new HashSet<>();
    private Set<Village> banishedVillages = new HashSet<>();
    private Set<Empire> banishedEmpires = new HashSet<>();

    public Empire(String name, IdObject capital) {
        this.name = name;
        this.capital = capital;
        members.add(capital);
    }

    public void doUpkeep() {
        if (getTax() > 0) {
            Utils.runAsync(() -> {
                int paidTaxes = 0;
                for (Village member : getMembers()) {
                    if (member.getId().equals(getCapitalId())) {
                        continue;
                    }
                    if (member.removeFromBank(member.getTax())) {
                        addToBank(getTax());
                        paidTaxes += getTax();
                        Message.get("empire-tax.removed").tell(member.getOnlineResidents(),
                                "amount", String.valueOf(getTax()),
                                "state", member.getState().getTag(),
                                "settlement", member.getName(),
                                "name", getName());
                    } else {
                        Message.get("empire-tax.couldnt-pay").tell(getOnlineResidents(),
                                "amount", String.valueOf(getTax()),
                                "state", member.getState().getTag(),
                                "settlement", member.getName(),
                                "name", getName()
                        );
                        member.setEmpire(null);
                        getMemberIds().remove(member.getId());
                    }
                }
                DatabaseManager.getInstance().saveVillages(getMembers());
                DatabaseManager.getInstance().log(this, null, null, "tax", "cash", paidTaxes);
            });
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", getName());
        map.put("capital", getCapitalId().value());
        map.put("description", getDescription());
        map.put("symbol", getSymbol());
        map.put("members", getMemberIds().stream().map(IdObject::value).filter(Objects::nonNull).collect(Collectors.toList()));
        map.put("senator", getSenatorIds().stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("pvp", isPvp());
        map.put("fireSpread", isFireSpread());
        map.put("explosions", isExplosions());
        map.put("mobSpawning", isMobSpawning());
        map.put("buff", isBuff());
        map.put("bank", getBank());
        map.put("joinFee", getJoinFee());
        map.put("tax", getTax());
        map.put("bonusClaims", getBonusClaims());
        map.put("buildState", getBuildState().name());
        if (getHome() != null)
            map.put("homeLoc", getHome().serialize());
        List<String> banished = new ArrayList<>();
        getBanishedPlayers().stream().map(UUID::toString).forEach(banished::add);
        getBanishedVillages().stream().map(v -> "village:" + v.getName()).forEach(banished::add);
        getBanishedEmpires().stream().map(e -> "empire:" + e.getName()).forEach(banished::add);
        map.put("banished", banished);
        map.put("uses", getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
        map.put("vault", getVault());
        map.put("perks", getPerksMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().serialize())));
        return map;
    }

    public static Empire deserialize(Map<String, Object> map) {
        if (map == null || map.get("name") == null || map.get("capital") == null || map.get("level") == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize Empire! (" + map + ")");
            return null;
        }
        Empire empire = new Empire(
                (String) map.get("name"),
                IdObject.get(map.get("capital"))
        );
        empire.setDescription((String) map.get("description"));
        empire.setSymbol((String) map.get("symbol"));
        ((Collection<String>) map.get("senators")).stream().map(UUID::fromString).forEach(u -> empire.getSenatorIds().add(u));
        if (map.get("members") != null && map.get("members") instanceof Collection) {
            for (Object memberId : (Collection) map.get("members")) {
                empire.members.add(IdObject.get(memberId));
            }
        }
        empire.setId(IdObject.get(map));
        empire.setBank((Long) map.get("bank"));
        empire.setJoinFee((Long) map.get("joinFee"));
        empire.setTax((Long) map.get("tax"));
        empire.setPvp((Boolean) map.get("pvp"));
        empire.setFireSpread((Boolean) map.get("fireSpread"));
        empire.setExplosions((Boolean) map.get("explosions"));
        empire.setMobSpawning((Boolean) map.get("mobSpawning"));
        empire.setBuff((Boolean) map.get("buff"));
        empire.setBonusClaims((Integer) map.get("bonusClaims"));
        if (map.get("homeLoc") != null) {
            empire.setHome(SimpleLocation.deserialize((Map<String, Object>) map.get("homeLoc")));
        } else if (map.get("homeLoc_x") != null && map.get("homeLoc_y") != null && map.get("homeLoc_Z") != null
                && map.get("homeLoc_world") != null && map.get("homeLoc_yaw") != null && map.get("homeLoc_pitch") != null){
            empire.setHome(new SimpleLocation(
                    (String) map.get("homeLoc_world"),
                    (double) map.get("homeLoc_x"),
                    (double) map.get("homeLoc_y"),
                    (double) map.get("homeLoc_z"),
                    (float) map.get("homeLoc_yaw"),
                    (float) map.get("homeLoc_pitch")
            ));
        }
        Map<String, Object> uses = (Map<String, Object>) map.get("uses");
        for (Use u : Use.values()) {
            if (uses.containsKey(u.name())) {
                empire.setUseState(u, Utils.parseEnum((String) uses.get(u.name()), u.getDefault(BuildState.PRIVATE)));
            }
        }
        ((Collection<String>) map.get("banished")).stream().filter(s -> !s.contains(":")).map(UUID::fromString).forEach(empire.banishedPlayers::add);
        ((Collection<String>) map.get("banished")).stream()
                .filter(s -> s.startsWith("village:"))
                .map(s -> {
                    String sub = s.split(":")[1];
                    Village v = null;
                    try {
                        v = VillageManager.getInstance().getVillageFromId(IdObject.get(Integer.parseInt(sub)));
                    } catch (NumberFormatException ignored) {}
                    if (v == null) {
                        v = VillageManager.getInstance().getVillageFromName(sub);
                    }
                    return v;
                })
                .filter(Objects::nonNull)
                .forEach(empire.banishedVillages::add);
        ((Collection<String>) map.get("banished")).stream()
                .filter(s -> s.startsWith("empire:"))
                .map(s -> {
                    String sub = s.split(":")[1];
                    Empire e = null;
                    try {
                        e = EmpireManager.getInstance().getEmpireFromId(IdObject.get(Integer.parseInt(sub)));
                    } catch (NumberFormatException ignored) {}
                    if (e == null) {
                        e = EmpireManager.getInstance().getEmpireFromName(sub);
                    }
                    return e;
                })
                .filter(Objects::nonNull)
                .forEach(empire.banishedEmpires::add);
        ((Collection<String>) map.get("banished")).stream().filter(s -> !s.contains(":")).map(UUID::fromString).forEach(empire.banishedPlayers::add);
        ((Map<String, Object>) map.get("vault")).entrySet().stream()
                .filter(e -> !(e.getValue() instanceof Integer))
                .forEach(e -> empire.getVault().put(e.getKey(), (Long) e.getValue()));
        ((Map<String, Object>) map.get("perks")).entrySet().stream()
                .filter(e -> !(e.getValue() instanceof Map))
                .forEach(e -> empire.getPerksMap().put(e.getKey(), PerkData.deserialize((Map<String, Object>) e.getValue())));

        return empire;
    }

    @Override
    public int compareTo(Empire e) {
        if (e.getMemberIds().size() != getMemberIds().size())
            return Integer.compare(e.getMemberIds().size(), getMemberIds().size());
        return Long.compare(e.getBank(), getBank());
    }

    @Override
    public int getBonusClaims() {
        return bonusClaims;
    }

    public int getBonusClaimAmount() {
        return getBonusClaims() + getPerksMap().entrySet().stream().mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusClaims()).sum();
    }

    public int getBonusOutposts() {
        return getPerksMap().entrySet().stream().mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusOutposts()).sum();
    }

    @Override
    public void setBonusClaims(int bonusClaims) {
        this.bonusClaims = bonusClaims;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Village getCapital() {
        return VillageManager.getInstance().getVillageFromId(capital);
    }

    public IdObject getCapitalId() {
        return capital;
    }

    public void setCapital(Village capital) {
        this.capital = capital.getId();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public Map<String, PerkData> getPerksMap() {
        return perks;
    }

    @Override
    public Perk getPerk(String name) {
        return EmpireManager.getInstance().getPerk(name);
    }

    public Set<IdObject> getMemberIds() {
        return members;
    }

    public Collection<Village> getMembers() {
        return getMemberIds().stream().map(i -> VillageManager.getInstance().getVillageFromId(i)).collect(Collectors.toList());
    }

    public boolean isFull() {
        return getMemberIds().size() >= ConfigManager.getInstance().getConfig().getInt("max-empire-size");
    }

    private Collection<UUID> getSenatorIds() {
        return senators;
    }

    public Collection<Resident> getSenators() {
        return getSenatorIds().stream().map(u -> ResidentManager.getInstance().getResident(u)).collect(Collectors.toList());
    }
    public BuildState getUseState(Use u) {
        return use.getOrDefault(u, u.getDefault(BuildState.PRIVATE));
    }

    @Override
    public boolean isChief(Player player) {
        return getCapital().isChief(player);
    }

    @Override
    public boolean isOfficer(Player player) {
        Resident res = ResidentManager.getInstance().getResident(player);
        return isMember(res) && res.getEmpireRank().atLeast(EmpireRank.SENATOR);
    }

    public void setUseState(Use u, BuildState state) {
        if (state == u.getDefault(BuildState.PRIVATE)) {
            use.remove(u);
        } else {
            use.put(u, state);
        }
    }

    public Map<Use, BuildState> getUseStates() {
        return use;
    }

    @Override
    public Map<String, Long> getVault() {
        return vault;
    }

    public long getBank() {
        return bank;
    }

    public void setBank(long bank) {
        this.bank = bank;
    }

    public IdObject getId() {
        return id;
    }

    public void setId(IdObject id) {
        this.id = id;
    }

    public boolean isCapital(Village village) {
        return capital.equals(village.getId());
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public List<Resident> getResidents() {
        return getMembers().stream().flatMap(v -> v.getResidents().stream()).collect(Collectors.toList());
    }

    @Override
    public List<Resident> getOnlineResidents() {
        return getMembers().stream().flatMap(v -> v.getOnlineResidents().stream()).collect(Collectors.toList());
    }

    @Override
    public boolean canAdminPlots(Resident resident) {
        return isMember(resident) && resident.getEmpireRank().atLeast(EmpireRank.LEADER);
    }

    @Override
    public boolean isMember(Resident resident) {
        return resident.hasEmpire() && getMembers().stream().anyMatch(v -> v.isMember(resident));
    }

    @Override
    public boolean isBanished(UUID playerId) {
        if (banishedPlayers.contains(playerId)) {
            return false;
        }
        Resident resident = ResidentManager.getInstance().getResident(playerId);
        if (resident != null && resident.hasVillage()) {
            if (banishedVillages.contains(resident.getVillage())) {
                return true;
            }
            if (resident.hasEmpire() && banishedEmpires.contains(resident.getVillage().getEmpire())) {
                return true;
            }
        }
        return false;
    }

    public boolean addBanished(Targetable target) {
        if (target instanceof Resident) {
            return banishedPlayers.add(((Resident) target).getUUID());
        } else if (target instanceof Village) {
            return banishedVillages.add((Village) target);
        } else if (target instanceof Empire) {
            return banishedEmpires.add((Empire) target);
        }
        return false;
    }

    public boolean removeBanished(Targetable target) {
        if (target instanceof Resident) {
            return banishedPlayers.remove(((Resident) target).getUUID());
        } else if (target instanceof Village) {
            return banishedVillages.remove((Village) target);
        } else if (target instanceof Empire) {
            return banishedEmpires.remove((Empire) target);
        }
        return false;
    }

    public boolean isPvp() {
        return pvp;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    public boolean isFireSpread() {
        return fireSpread;
    }

    public void setFireSpread(boolean fireSpread) {
        this.fireSpread = fireSpread;
    }

    public boolean isExplosions() {
        return explosions;
    }

    public void setExplosions(boolean explosions) {
        this.explosions = explosions;
    }

    public boolean isMobSpawning() {
        return mobSpawning;
    }

    public void setMobSpawning(boolean mobSpawning) {
        this.mobSpawning = mobSpawning;
    }

    @Override
    public boolean isBuff() {
        return buff;
    }

    public void setBuff(boolean buff) {
        this.buff = buff;
    }

    public SimpleLocation getHome() {
        return home;
    }

    @Override
    public String getStateKey() {
        return State.EMPIRE.getKey();
    }

    @Override
    public String getStateTag() {
        return State.EMPIRE.getTag();
    }

    public Set<UUID> getBanishedPlayers() {
        return banishedPlayers;
    }

    public Set<Village> getBanishedVillages() {
        return banishedVillages;
    }

    public Set<Empire> getBanishedEmpires() {
        return banishedEmpires;
    }

    private enum State implements NameTag {
        EMPIRE;
    }

    public void setHome(SimpleLocation home) {
        this.home = home;
    }

    public void setHome(Location location) {
        setHome(new SimpleLocation(location));
    }

    public long getJoinFee() {
        return joinFee;
    }

    public void setJoinFee(long joinFee) {
        this.joinFee = joinFee;
    }

    public long getTax() {
        return tax;
    }

    public void setTax(long tax) {
        this.tax = tax;
    }

    public void setBuildState(BuildState buildState) {
        this.buildState = buildState;
    }

    @Override
    public BuildState getBuildState() {
        return this.buildState;
    }

    public String getWorldName() {
        return KingdomsPlugin.getInstance().getEmpiresWorldPrefix() + getName() + KingdomsPlugin.getInstance().getEmpiresWorldSuffix();
    }

    public boolean isMember(Village village) {
        return village != null && getMemberIds().contains(village.getId());
    }

    @Override
    public boolean isAlly(LandOwner landOwner) {
        return landOwner instanceof Village && getMembers().stream().anyMatch(v -> v.isAlly(landOwner));
    }

    @Override
    public boolean isTrusted(LandOwner landOwner) {
        return landOwner instanceof Village && (isAlly(landOwner) || (isMember((Village) landOwner)));
    }

    public boolean canAccess(Player player) {
        return canAccess(ResidentManager.getInstance().getResident(player));
    }

    public boolean canAccess(Resident resident) {
        if (getBuildState().atLeast(BuildState.PRIVATE)) {
            return isMember(resident.getVillage());
        }
        if (getBuildState().atLeast(BuildState.ALLY)) {
            return isAlly(resident.getVillage());
        }
        if (getBuildState().atLeast(BuildState.PUBLIC)) {
            return true;
        }
        return false;
    }

    public String[] getReplacements(ConfigMessages configMessages) {
        Village capital = getCapital();
        Resident chief = capital.getChief();
        return new String[] {
                "name", getName(),
                "description", getDescription(),
                "symbol", getSymbol(),
                "pvp", yesOrNo(isPvp()),
                "balance", Utils.CURRENCY_FORMAT.format(getBank()),
                "joinfee", Utils.CURRENCY_FORMAT.format(getBank()),
                "tax", Utils.CURRENCY_FORMAT.format(getBank()),
                "mobspawning", yesOrNo(isMobSpawning()),
                "firespread", yesOrNo(isFireSpread()),
                "explosions", yesOrNo(isExplosions()),
                "buff", yesOrNo(isBuff()),
                "bonusoutposts", String.valueOf(getBonusOutposts()),
                "bonusclaims", String.valueOf(getBonusClaimAmount()),
                "capital", capital.getName(),
                "capitalstate", capital.getState().getTag(),
                "perks", getPerksMap().entrySet().stream()
                        .map(e -> (e.getValue().getTimes() > 1 ? e.getValue().getTimes() + "x " : "") + getPerk(e.getKey()).getTag())
                        .collect(Collectors.joining(", ")),
                "chiefrank", VillageRank.CHIEF.getTag(capital),
                "chieftitle", chief.getTitle(),
                "chief", chief.getName(),
                "senators", getSenators().stream().map(r -> r.getTitle() + " " + r.getName()).collect(Collectors.joining(", ")),
                "members", getMembers().stream().map(v -> v.getState().getTag() + " " + v.getName()).collect(Collectors.joining(", "))
        };
    }
}
