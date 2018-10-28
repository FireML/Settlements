package me.crz.minecraft.kingdoms.empire;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.perk.Perkable;
import me.crz.minecraft.kingdoms.utils.BonusClaimsHolder;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.vault.Levels;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Collection;
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

public class Empire implements ConfigurationSerializable, Comparable<Empire>, BonusClaimsHolder, Perkable<Perk> {
    private IdObject id = IdObject.get(null);
    private String name;
    private IdObject capital;
    private String description = "";
    private int level;
    private long bank = 0;
    private boolean pvp = false;
    private boolean fireSpread = false;
    private boolean explosions = false;
    private boolean mobSpawning = false;
    private boolean buff = false;
    private Set<IdObject> members = new LinkedHashSet<>();
    private Set<UUID> senators = new HashSet<>();
    private int bonusClaims = 0;
    private Map<String, Long> vault = new HashMap<>();
    private Map<String, PerkData> perks = new LinkedHashMap<>();
    private SimpleLocation home;

    public Empire(String name, IdObject capital, int level) {
        this.name = name;
        this.capital = capital;
        this.level = level;
        members.add(capital);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", getName());
        map.put("capital", getCapitalId().value());
        map.put("description", getDescription());
        map.put("members", getMemberIds().stream().map(IdObject::value).filter(Objects::nonNull).collect(Collectors.toList()));
        map.put("senator", getSenatorIds().stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("pvp", isPvp());
        map.put("fireSpread", isFireSpread());
        map.put("explosions", isExplosions());
        map.put("mobSpawning", isMobSpawning());
        map.put("buff", isBuff());
        map.put("bank", getBank());
        map.put("bonusClaims", getBonusClaims());
        if (getHome() != null)
            map.put("homeLoc", getHome().serialize());
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
                IdObject.get(map.get("capital")),
                (Integer) map.get("level")
        );
        empire.setDescription((String) map.get("description"));
        ((Collection<String>) map.get("senators")).stream().map(UUID::fromString).forEach(u -> empire.getSenatorIds().add(u));
        if (map.get("members") != null && map.get("members") instanceof Collection) {
            for (Object memberId : (Collection) map.get("members")) {
                empire.members.add(IdObject.get(memberId));
            }
        }
        empire.setId(IdObject.get(map));
        empire.setBank((Long) map.get("bank"));
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
        if (e.getLevel() != getLevel())
            return Integer.compare(e.getLevel(), getLevel());
        if (e.getMemberIds().size() != getMemberIds().size())
            return Integer.compare(e.getMemberIds().size(), getMemberIds().size());
        return Long.compare(e.getBank(), getBank());
    }

    @Override
    public int getBonusClaims() {
        return bonusClaims;
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

    public int getLevel() {
        return level;
    }

    public boolean canLevelUp() {
        int currentLevel = getLevel();
        if (isMaxLevel()) {
            return false;
        }
        return has(Levels.getEmpireLevelRequirements(currentLevel + 1));
    }

    public boolean isMaxLevel() {
        return getLevel() == Levels.EMPIRES_MAX_LEVEL || Levels.getEmpireLevelRequirements(getLevel() + 1) == null;
    }

    @Override
    public Map<String, PerkData> getPerksMap() {
        return perks;
    }

    @Override
    public Perk getPerk(String name) {
        return EmpireManager.getInstance().getPerk(name);
    }

    @Override
    public void runCommands(Perk perk) {
        // TODO: Where should commands run?
    }

    public void setLevel(int level) {
        this.level = level;
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

    public List<Resident> getOnlineResidents() {
        return getMembers().stream().flatMap(v -> v.getOnlineResidents().stream()).collect(Collectors.toList());
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

    public boolean isBuff() {
        return buff;
    }

    public void setBuff(boolean buff) {
        this.buff = buff;
    }

    public SimpleLocation getHome() {
        return home;
    }

    public void setHome(SimpleLocation home) {
        this.home = home;
    }

    public void setHome(Location location) {
        setHome(new SimpleLocation(location));
    }
}
