package me.crz.minecraft.kingdoms.empire;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.perk.Perk;
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
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static me.crz.minecraft.kingdoms.utils.MessageUtils.yesOrNo;

public class Empire extends LandOwner implements Comparable<Empire>, Perkable<Perk> {
    private IdObject capital;
    private String symbol = "";
    private Set<IdObject> members = new LinkedHashSet<>();
    private Set<UUID> senators = new HashSet<>();

    protected Empire() {
        super();
    }

    public Empire(String name, IdObject capital) {
        super(name);
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
        Map<String, Object> map = super.serialize();
        map.put("capital", getCapitalId().value());
        map.put("symbol", getSymbol());
        map.put("members", getMemberIds().stream().map(IdObject::value).filter(Objects::nonNull).collect(Collectors.toList()));
        map.put("senator", getSenatorIds().stream().map(UUID::toString).collect(Collectors.toList()));
        return map;
    }

    @Override
    public void removeMember(Targetable target) {
        super.removeMember(target);
        if (target instanceof Village && isMember((Village) target)) {
            EmpireManager.getInstance().leaveEmpire((Village) target);
        }
    }

    public static Empire deserialize(Map<String, Object> map) {
        Empire empire = LandOwner.deserialize(new Empire(), map);
        if (empire == null) {
            return null;
        }
        if (map == null || map.get("capital") == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize Empire! (" + map + ")");
            return null;
        }
        empire.capital = IdObject.get(map.get("capital"));
        empire.setSymbol((String) map.get("symbol"));
        ((Collection<String>) map.get("senators")).stream().map(UUID::fromString).forEach(u -> empire.getSenatorIds().add(u));
        if (map.get("members") != null && map.get("members") instanceof Collection) {
            for (Object memberId : (Collection) map.get("members")) {
                empire.members.add(IdObject.get(memberId));
            }
        }
        return empire;
    }

    @Override
    public int compareTo(Empire e) {
        if (e.getMemberIds().size() != getMemberIds().size())
            return Integer.compare(e.getMemberIds().size(), getMemberIds().size());
        return Long.compare(e.getBank(), getBank());
    }

    public int getBonusClaimAmount() {
        return getBonusClaims() + getPerksMap().entrySet().stream().mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusClaims()).sum();
    }

    public int getBonusOutposts() {
        return getPerksMap().entrySet().stream().mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusOutposts()).sum();
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

    @Override
    public boolean isChief(UUID playerId) {
        return getCapital().isChief(playerId);
    }

    @Override
    public UUID getChiefId() {
        return getCapital().getChiefId();
    }

    @Override
    public boolean isChief(Targetable target) {
        if (super.isChief(target)) {
            return true;
        }
        if (target instanceof Village) {
            return isCapital((Village) target);
        }
        return false;
    }

    @Override
    public boolean isOfficer(Player player) {
        Resident res = ResidentManager.getInstance().getResident(player);
        return isMember(res) && res.getEmpireRank().atLeast(EmpireRank.SENATOR);
    }

    public boolean isCapital(Village village) {
        return capital.equals(village.getId());
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
    public String getStateKey() {
        return State.EMPIRE.getKey();
    }

    @Override
    public String getStateTag() {
        return State.EMPIRE.getTag();
    }

    private enum State implements NameTag {
        EMPIRE;
    }

    public String getWorldName() {
        return KingdomsPlugin.getInstance().getEmpiresWorldPrefix() + getName() + KingdomsPlugin.getInstance().getEmpiresWorldSuffix();
    }

    public boolean isMember(Village village) {
        return village != null && getMemberIds().contains(village.getId());
    }

    @Override
    public boolean isTrusted(LandOwner landOwner) {
        return landOwner instanceof Village && (isAlly(landOwner) || (isMember((Village) landOwner)));
    }

    public boolean canAccess(Player player) {
        return canAccess(ResidentManager.getInstance().getResident(player));
    }

    public boolean canAccess(Resident resident) {
        if (getAccess().atLeast(LandAccess.PRIVATE)) {
            return isMember(resident.getVillage());
        }
        if (getAccess().atLeast(LandAccess.ALLY)) {
            return isAlly(resident.getVillage());
        }
        if (getAccess().atLeast(LandAccess.PUBLIC)) {
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
                "access", getAccess().getTag(),
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
