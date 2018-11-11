package me.crz.minecraft.kingdoms.village;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.perk.LevelPerk;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.utils.config.Data;
import me.crz.minecraft.kingdoms.vault.Levels;
import me.crz.minecraft.kingdoms.world.NamedLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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

public class Village extends LandOwner implements Comparable<Village>, Targetable {
    private int level;
    private UUID chief;
    private Empire empire;
    private VillageState villageState;
    private Map<String, NamedLocation> outpostHomes = new LinkedHashMap<>();
    private Set<UUID> residents = new LinkedHashSet<>();
    private boolean allowAllyClaim = false;

    protected Village() {
        super();
    }

    public Village(String name, UUID chief, int level, VillageState state) {
        super(name);
        residents.add(chief);
        this.chief = chief;
        this.level = level;
        this.villageState = state;
    }

    public void doUpkeep() {
        if (getTax() > 0) {
            Utils.runAsync(() -> {
                long totalTax = 0;
                for (Resident resident : getResidents()) {
                    if (resident.getUUID().equals(getChiefId())) {
                        continue;
                    }
                    OfflinePlayer player = resident.getPlayer();
                    if (player == null) {
                        player = Bukkit.getOfflinePlayer(resident.getUUID());
                    }
                    if (KingdomsPlugin.getInstance().econ.withdrawPlayer(player, getTax()).transactionSuccess()) {
                        addToBank(getTax());
                        totalTax += getTax();
                        Message.get("tax.removed").tell(resident,
                                "amount", String.valueOf(getTax()),
                                "state", getState().getTag(),
                                "name", getName());
                    } else {
                        Message.get("tax.couldnt-pay").tell(getOnlineResidents(),
                                "player", resident.getName(),
                                "amount", String.valueOf(getTax()),
                                "state", getState().getTag(),
                                "name", getName()
                        );
                        addBanished(resident);
                        VillageManager.getInstance().leaveVillage(resident);
                    }
                }
                long finalTotalTax = totalTax;
                Utils.runSync(() -> payUpkeep(finalTotalTax));
                DatabaseManager.getInstance().log(this, null, "tax", "cash", totalTax);
            });
        } else {
            payUpkeep(0);
        }
    }

    private void payUpkeep(long lastTax) {
        if (hasEmpire() && getEmpire().isCapital(this)) {
            // no upkeep for empire capitals
            if (lastTax > 0) {
                DatabaseManager.getInstance().saveVillage(this);
            }
            return;
        }
        int total = getUpkeep();
        if (removeFromBank(total)) {
            KingdomsPlugin.getInstance().getLogger().log(Level.INFO, getState().getTag() + " " + getName() + " (level " + getLevel() + ") was charged an upkeep of " + total + "!");
            MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.charged"), "charged", String.valueOf(total));
            if (!has(total - lastTax)) {
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.cant-pay"), "cost", String.valueOf(total));
            }
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveVillage(this);
                DatabaseManager.getInstance().log(this, null, "upkeep", "cash", total);
            });
        } else {
            if (getLevel() > 1 || getState() == VillageState.KINGDOM) {
                if (getLevel() == 1 && getState() == VillageState.KINGDOM) {
                    KingdomsPlugin.getInstance().getLogger().log(Level.INFO, getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and has been downgraded!");
                    setState(VillageState.VILLAGE);
                    setLevel(Levels.VILLAGE_TO_KINGDOM_LEVEL);
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.downgraded"), "state", getState().getTag(), "level", String.valueOf(getLevel()));
                } else {
                    KingdomsPlugin.getInstance().getLogger().log(Level.INFO, getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and has had a level removed!");
                    setLevel(getLevel() - 1);
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.level-removed"), "level", String.valueOf(getLevel()));
                }
                Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(this));
            } else {
                KingdomsPlugin.getInstance().getLogger().log(Level.INFO, getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and was deleted!");
                setBank(0);
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.deleted"));
                VillageManager.getInstance().deleteVillage(this);
            }
        }
    }

    public int getUpkeep() {
        return LandManager.getInstance().getClaimedLandSize(this) * Utils.getUpkeepCost();
    }

    public void setOutpostHome(String name, Location loc) {
        setOutpostHome(new NamedLocation(name, loc));
    }

    public void setOutpostHome(NamedLocation location) {
        outpostHomes.put(location.getName().toLowerCase(), location);
    }

    public Map<String, NamedLocation> getOutpostHomes() {
        return outpostHomes;
    }

    public boolean hasHome() {
        return this.getHome() != null;
    }

    public boolean isAllowAllyClaim() {
        return allowAllyClaim;
    }

    public void setAllowAllyClaim(boolean allowAllyClaim) {
        this.allowAllyClaim = allowAllyClaim;
    }

    @Override
    public boolean isTrusted(LandOwner landOwner) {
        return isAlly(landOwner)
                || (landOwner instanceof Empire && landOwner.equals(getEmpire()))
                || (landOwner instanceof Village && hasEmpire() && getEmpire().isMember((Village) landOwner));
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        if (Data.first.getString(villageState.getKey() + "-" + level).equals("")) {
            Data.first.put(villageState.getKey() + "-" + level, this.getName());
            KingdomsPlugin.data.save(KingdomsPlugin.getInstance().getDataFolder());
            for (Player player : Bukkit.getOnlinePlayers()) {
                MessageUtils.tell(player, Message.get("first-to-reach-level"),
                        "name", this.getName(),
                        "state", villageState.name().toLowerCase(),
                        "level", String.valueOf(level));
            }
        }
    }

    @Override
    public boolean isChief(UUID playerId) {
        return chief.equals(playerId);
    }

    public UUID getChiefId() {
        return chief;
    }

    public void setChief(UUID chief) {
        this.chief = chief;
    }

    /**
     * Get the chief, might run database operation!
     * @return The chief's Resident object
     */
    public Resident getChief() {
        return ResidentManager.getInstance().getResident(getChiefId());
    }

    /**
     * Might run database operation!
     */
    @Override
    public List<Resident> getResidents() {
        return residents.stream()
                .map(id -> ResidentManager.getInstance().getResident(id)).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Resident> getOnlineResidents() {
        return residents.stream()
                .map(Bukkit::getPlayer).filter(Objects::nonNull)
                .map(p -> ResidentManager.getInstance().getResident(p))
                .collect(Collectors.toList());
    }

    @Override
    public boolean canAdminPlots(Resident resident) {
        return isMember(resident) && resident.getVillageRank().atLeast(VillageRank.ELDER);
    }

    @Override
    public boolean isOfficer(Player player) {
        Resident res = ResidentManager.getInstance().getResident(player);
        return isMember(res) && res.getVillageRank().atLeast(VillageRank.BUILDER);
    }

    @Override
    public boolean isMember(Resident resident) {
        return residents.contains(resident.getUUID());
    }

    @Override
    public String getStateTag() {
        return getState().getTag();
    }

    @Override
    public String getStateKey() {
        return getState().getKey();
    }

    public Set<UUID> getResidentIds() {
        return residents;
    }

    public boolean isFull() {
        return this.getResidentIds().size() >= ConfigManager.getInstance().getConfig().getInt("max-village-size");
    }

    public VillageState getState() {
        return villageState;
    }

    public void setState(VillageState state) {
        this.villageState = state;
    }

    public int getMaxClaimableChunks() {
        return Utils.getLevelChunks(getState(), getLevel()) + getBonusClaimAmount();
    }

    public int getBonusClaimAmount() {
        return getBonusClaims() + getPerksMap().entrySet().stream().mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusClaims()).sum()
                + (hasEmpire() ? getEmpire().getBonusClaimAmount() : 0);
    }

    public int getMaxOutposts() {
        return Utils.getLevelOutposts(getState(), getLevel()) + getBonusOutposts() + (hasEmpire() ? getEmpire().getBonusOutposts() : 0);
    }

    public int getBonusOutposts() {
        return getPerksMap().entrySet().stream().mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusOutposts()).sum();
    }

    public Empire getEmpire() {
        return empire;
    }

    public void setEmpire(Empire empire) {
        if (getEmpire() != null) {
            getEmpire().getMemberIds().remove(getId());
        }
        this.empire = empire;
        if (empire != null) {
            empire.getMemberIds().add(getId());
        }
    }

    public boolean hasEmpire() {
        return empire != null;
    }

    public boolean canLevelUp() {
        if (isMaxLevel()) {
            return false;
        }
        return has(getNextLevelRequirements());
    }

    public Map<String, Long> getNextLevelRequirements() {
        int nextLevel = getLevel() + 1;
        VillageState nextState = getState();
        if (nextState == VillageState.VILLAGE && nextLevel > Levels.VILLAGE_TO_KINGDOM_LEVEL) {
            nextState = VillageState.KINGDOM;
            nextLevel = 1;
        }
        return Levels.getLevelRequirements(nextState, nextLevel);
    }

    public boolean isMaxLevel() {
        return (getLevel() == Levels.KINGDOMS_MAX_LEVEL && getState() == VillageState.KINGDOM) || getNextLevelRequirements() == null;
    }

    @Override
    public LevelPerk getPerk(String name) {
        return VillageManager.getInstance().getPerk(name);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("chief", getChiefId());
        map.put("level", getLevel());
        map.put("allowAllyClaim", isAllowAllyClaim());
        map.put("settlementState", getState().name());
        map.put("residents", getResidentIds().stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("outposts", getOutpostHomes().values().stream().collect(Collectors.toMap(NamedLocation::getName, NamedLocation::serialize)));
        return map;
    }

    @Override
    public void removeMember(Targetable target) {
        super.removeMember(target);
        if (target instanceof Resident && isMember((Resident) target)) {
            VillageManager.getInstance().leaveVillage((Resident) target);
        }
    }

    public static Village deserialize(Map<String, Object> map) {
        Village village = LandOwner.deserialize(new Village(), map);
        if (village == null) {
            return null;
        }
        if (map == null || map.get("chief") == null || map.get("level") == null || map.get("settlementState") == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize Village! (" + map + ")");
            return null;
        }
        village.setChief(UUID.fromString((String) map.get("chief")));
        village.setState(VillageState.valueOf((String) map.get("settlementState")));
        village.setLevel((Integer) map.get("level"));
        village.setAllowAllyClaim((Boolean) map.get("allowAllyClaim"));
        village.setEmpire(EmpireManager.getInstance().getEmpireFromId(IdObject.get(map.get("empireId"))));
        ((Collection<String>) map.get("residents")).stream().map(UUID::fromString).forEach(u -> village.getResidentIds().add(u));
        ((Map<String, Map<String, Object>>) map.get("outposts")).values().stream()
                .map(NamedLocation::deserialize)
                .filter(Objects::nonNull)
                .forEach(village::setOutpostHome);
        return village;
    }

    @Override
    public int compareTo(Village v) {
        if (v.getState() != getState())
            return v.getState().compareTo(getState());
        if (v.getLevel() != getLevel())
            return Integer.compare(v.getLevel(), getLevel());
        if (v.getResidentIds().size() != getResidentIds().size())
            return Integer.compare(v.getResidentIds().size(), getResidentIds().size());
        return Long.compare(v.getBank(), getBank());
    }

    public String[] getReplacements(ConfigMessages configMessages) {
        Resident chief = getChief();
        return new String[] {
                "name", getName(),
                "description", getDescription(),
                "state", getState().getTag(),
                "level", String.valueOf(getLevel()),
                "land", String.valueOf(LandManager.getInstance().getClaimedLandSize(this)),
                "maxland", String.valueOf(getMaxClaimableChunks()),
                "outposts", String.valueOf(getOutpostHomes().size()),
                "maxoutposts", String.valueOf(getMaxOutposts()),
                "pvp", yesOrNo(isPvp()),
                "balance", Utils.CURRENCY_FORMAT.format(getBank()),
                "tax", Utils.CURRENCY_FORMAT.format(getTax()),
                "joinfee", Utils.CURRENCY_FORMAT.format(getJoinFee()),
                "allowallyclaim", yesOrNo(isAllowAllyClaim()),
                "mobspawning", yesOrNo(isMobSpawning()),
                "firespread", yesOrNo(isFireSpread()),
                "explosions", yesOrNo(isExplosions()),
                "buff", yesOrNo(isBuff()),
                "bank", String.valueOf(getBank()),
                "bonusclaims", getBonusClaimAmount() > 0 ? configMessages.getMessage("bonus-claims").getReplacedText("amount", String.valueOf(getBonusClaimAmount())) : "",
                "bonusoutposts", getBonusOutposts() > 0 ? configMessages.getMessage("bonus-outposts").getReplacedText("amount", String.valueOf(getBonusOutposts())) : "",
                "access", getAccess().getTag(),
                "perks", getPerksMap().entrySet().stream()
                        .map(e -> (e.getValue().getTimes() > 1 ? e.getValue().getTimes() + "x " : "") + getPerk(e.getKey()).getTag())
                        .collect(Collectors.joining(", ")),
                "chiefrank", VillageRank.CHIEF.getTag(this),
                "chieftitle", chief.getTitle(),
                "chief", chief.getName(),
                "residents", getResidentsToString(configMessages)
        };
    }

    private String getResidentsToString(ConfigMessages configMessages) {
        Message rankMessage = configMessages.getMessage("residents.rank");
        String entry = configMessages.getMessage("residents.entry").getText();
        String online = configMessages.getMessage("residents.online").getText();
        String offline = configMessages.getMessage("residents.offline").getText();
        String separator = configMessages.getMessage("residents.separator").getText();
        Multimap<VillageRank, Resident> rankMap = MultimapBuilder.enumKeys(VillageRank.class).arrayListValues().build();
        for (Resident resident : getResidents()) {
            rankMap.put(resident.getVillageRank(), resident);
        }
        List<String> lines = new ArrayList<>();
        for (VillageRank rank : rankMap.keySet()) {
            if (rank == VillageRank.CHIEF) {
                // should be listed extra
                continue;
            }
            lines.add(rankMessage.getReplacedText(
                    "rank", rank.getTag(this),
                    "residents", rankMap.get(rank).stream()
                            .sorted(Comparator.comparing(Resident::getVillageJoin))
                            .map(res -> MessageUtils.replacePlaceholders(entry,
                                    "online", res.getPlayer() != null ? online : offline,
                                    "name", res.getName(),
                                    "rank", rank.getTag(this),
                                    "title", res.getTitle()
                            ))
                            .collect(Collectors.joining(separator))
            ));
        }
        return String.join("\n", lines);
    }
}
