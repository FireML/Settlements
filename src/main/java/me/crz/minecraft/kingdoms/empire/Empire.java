package me.crz.minecraft.kingdoms.empire;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.perk.Perkable;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.BonusClaimsHolder;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.utils.config.Data;
import me.crz.minecraft.kingdoms.vault.Levels;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

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

import static me.crz.minecraft.kingdoms.utils.MessageUtils.yesOrNo;

public class Empire implements ConfigurationSerializable, Comparable<Empire>, BonusClaimsHolder, Perkable<Perk> {
    private IdObject id = IdObject.get(null);
    private String name;
    private IdObject capital;
    private String description = "";
    private String symbol = "";
    private int level;
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

    public Empire(String name, IdObject capital, int level) {
        this.name = name;
        this.capital = capital;
        this.level = level;
        members.add(capital);
    }

    public void doUpkeep() {
        if (getTax() > 0) {
            Utils.runAsync(() -> {
                Map<Village, Long> paidTaxes = new LinkedHashMap<>();
                for (Village member : getMembers()) {
                    if (member.getId().equals(getCapitalId())) {
                        continue;
                    }
                    if (member.removeFromBank(member.getTax())) {
                        addToBank(getTax());
                        paidTaxes.put(member, getTax());
                        Message.get("empire-tax.removed").tell(member.getOnlineResidents(),
                                "amount", String.valueOf(getTax()),
                                "state", member.getState().getTag(),
                                "name", getName());
                    } else {
                        member.setEmpire(null);
                        getMemberIds().remove(member.getId());
                        paidTaxes.put(member, (long) 0);
                        Message.get("empire-tax.couldnt-pay").tell(member.getOnlineResidents(),
                                "amount", String.valueOf(getTax()),
                                "name", getName());
                    }
                }
                DatabaseManager.getInstance().saveVillages(paidTaxes.keySet());
                for (Map.Entry<Village, Long> e : paidTaxes.entrySet()) {
                    if (e.getValue() > 0) {
                        DatabaseManager.getInstance().log(this, e.getKey(), null, "tax", "cash", e.getValue());
                    }
                }
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
        map.put("bonusClaims", bonusClaims);
        map.put("buildState", getBuildState().name());
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
        return bonusClaims + getPerksMap().entrySet().stream().mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusClaims()).sum();
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
        if (Data.first.getString("empire-" + level).equals("")) {
            Data.first.put("empire-" + level, this.getName());
            KingdomsPlugin.data.save(KingdomsPlugin.getInstance().getDataFolder());
            for (Player player : Bukkit.getOnlinePlayers()) {
                Message.get("first-to-reach-level").tell(player,
                        "name", getName(),
                        "state", "Empire",
                        "level", String.valueOf(level));
            }
        }
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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<Resident> getOnlineResidents() {
        return getMembers().stream().flatMap(v -> v.getOnlineResidents().stream()).collect(Collectors.toList());
    }

    @Override
    public boolean isMember(Resident resident) {
        return getMembers().stream().anyMatch(v -> v.isMember(resident));
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

    public BuildState getBuildState() {
        return this.buildState;
    }

    public String getWorldName() {
        return KingdomsPlugin.getInstance().getEmpiresWorldPrefix() + getName();
    }

    public boolean isMember(Village village) {
        return village != null && getMemberIds().contains(village.getId());
    }

    public boolean isAlly(Village village) {
        return village != null && getMembers().stream().anyMatch(v -> v.isAlly(village));
    }

    public boolean canAccess(Player player) {
        return canAccess(ResidentManager.getInstance().getResident(player));
    }

    public boolean canAccess(Resident resident) {
        switch (getBuildState()) {
            case PRIVATE:
                return isMember(resident.getVillage());
            case ALLY:
                return isAlly(resident.getVillage());
            case PUBLIC:
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
                "level", String.valueOf(getLevel()),
                "pvp", yesOrNo(isPvp()),
                "balance", Utils.CURRENCY_FORMAT.format(getBank()),
                "joinfee", Utils.CURRENCY_FORMAT.format(getBank()),
                "tax", Utils.CURRENCY_FORMAT.format(getBank()),
                "mobspawning", yesOrNo(isMobSpawning()),
                "firespread", yesOrNo(isFireSpread()),
                "explosions", yesOrNo(isExplosions()),
                "buff", yesOrNo(isBuff()),
                "bonusclaims", getBonusClaims() > 0 ? configMessages.getMessage("bonus-claims").getReplacedText("amount", String.valueOf(getBonusClaims())) : "",
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
