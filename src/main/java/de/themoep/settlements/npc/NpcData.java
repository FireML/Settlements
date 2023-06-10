package de.themoep.settlements.npc;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.leveling.Levelable;
import de.themoep.settlements.perk.LevelPerk;
import de.themoep.settlements.perk.NpcPerk;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.HumanReadable;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.Message;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class NpcData implements DatabaseSerializable, Levelable<NpcLevel> {
    private IdObject id = IdObject.get(null);
    private LandOwner<?, ?> owner;
    private int npcId;
    private final NpcType type;
    private int level = 0;
    private String npcSkinName = "";
    private Map<String, Long> vault = new HashMap<>();
    private boolean enabled = false;

    public NpcData(int npcId, NpcType type) {
        this.npcId = npcId;
        this.type = type;
    }

    protected NpcData(IdObject id, int npcId, NpcType type, int level, String npcSkinName, boolean enabled) {
        this(npcId, type);
        this.id = id;
        this.level = level;
        this.npcSkinName = npcSkinName;
        this.enabled = enabled;
    }

    public boolean isUnlocked() {
        return level > 0;
    }

    @Override
    public IdObject getId() {
        return id;
    }

    @Override
    public void setId(IdObject id) {
        this.id = id;
    }

    @Override
    public void save() {
        DatabaseManager.getInstance().saveNpc(this);
    }

    @Override
    public LinkedHashMap<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("id", id.value());
        map.put(owner.getDbName() + "Id", owner.getId());
        map.put("npcId", npcId);
        map.put("type", type);
        map.put("level", level);
        map.put("npcSkinName", npcSkinName);
        map.put("vault", vault);
        map.put("enabled", enabled);
        return map;
    }

    public static NpcData deserialize(Map<String, Object> map) {
        NpcData data = new NpcData(
                IdObject.get(map.get("id")),
                (int) map.get("npcId"),
                NpcType.valueOf(((String) map.get("type")).toUpperCase(Locale.ROOT)),
                (int) map.get("level"),
                (String) map.get("npcSkinName"),
                (boolean) map.get("enabled")
        );
        ((Map<String, Object>) map.get("vault")).entrySet().stream()
                .filter(e -> e.getValue() instanceof Number)
                .forEach(e -> data.getVault().put(e.getKey(), Long.parseLong(String.valueOf(e.getValue()))));
        return data;
    }

    @Override
    public String toString() {
        return "NpcData{" +
                "id=" + id +
                ", npcId=" + npcId +
                ", type=" + type +
                ", level=" + level +
                ", npcSkinName='" + npcSkinName + '\'' +
                ", enabled=" + enabled +
                '}';
    }

    @Override
    public String name() {
        return type.getKey();
    }

    @Override
    public String getTag() {
        NpcConfig config = NpcManager.getInstance().getConfig(owner, type);
        return config != null ? config.getName() : type.getTag();
    }

    public int getNpcId() {
        return npcId;
    }

    public void setNpcId(int npcId) {
        this.npcId = npcId;
    }

    public NpcType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    @Nullable
    @Override
    public NpcLevel getLevelInfo() {
        return NpcManager.getInstance().getLevel(owner, type, level).orElse(null);
    }

    @Override
    public Optional<NpcLevel> getNextLevel() {
        return NpcManager.getInstance().getLevel(owner, type, level + 1);
    }

    @Override
    public boolean isMaxLevel() {
        return isUnlocked() && getNextLevelRequirements().isEmpty();
    }

    @Override
    public boolean checkLevelUp(Resident resident) {
        if (!canLevelUp()) {
            return false;
        }

        remove(getNextLevelRequirements());
        setLevel(getLevel() + 1);

        if (getLevel() > 1) {
            Message.get("levels.upgrade").tell(owner, "state", getTag(), "level", String.valueOf(level));
        }

        List<String> perksDesc = new ArrayList<>();
        for (String perkName : getLevelInfo().getPerks()) {
            NpcPerk perk = NpcManager.getInstance().getPerk(owner, perkName);
            if (perk != null) {
                owner.unlockPerk(resident, perk);
                for (String unlockable : perk.getUnlockables()) {
                    perksDesc.add(HumanReadable.humanizedName(unlockable));
                }
            }
        }

        String mail;
        if (level > 1) {
             mail = Message.get("levels.mail-upgrade.general").getReplacedText(
                    "name", getTag(),
                    "level", String.valueOf(level)
            );
        } else {
            mail = Message.get("levels.perk-unlocked").getReplacedText(
                    "perk", getTag()
            );
        }
        if (!perksDesc.isEmpty()) {
            mail += "\n" + Message.get("levels.mail-upgrade.perks").getReplacedText(
                    "perks", perksDesc.stream().map(p -> Message.get("levels.mail-upgrade.perks-entry").getReplacedText("perk", p))
                            .collect(Collectors.joining("\n"))
            );
            Message.get("levels.perks").tell(owner,
                    "perks", perksDesc.stream().map(p -> Message.get("levels.perks-entry").getReplacedText("perk", p))
                            .collect(Collectors.joining("\n")));
        }
        for (MailProvider provider : SettlementsPlugin.getInstance().getMailProviders()) {
            for (Resident r : owner.getResidents()) {
                provider.sendMail(owner.getStateTag() + " " + getTag(), r, mail);
            }
        }
        save();
        return true;
    }

    @Override
    public Collection<? extends LevelPerk> getPerks(int level) {
        return NpcManager.getInstance().getLevel(owner, type, level)
                .map(l -> l.getPerks().stream().map(p -> NpcManager.getInstance().getPerk(owner, p)).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public void logDeposit(Resident resident, Map<String, Long> deposited) {
        owner.logDeposit(resident, deposited); // TODO: Per NPC?
    }

    @Override
    public Collection<Resident> getOnlineResidents() {
        return owner.getOnlineResidents();
    }

    @Override
    public Map<String, Long> getVault() {
        return vault;
    }

    @Override
    public long getAmount(String key) {
        if ("missions".equalsIgnoreCase(key)
                || "missionpoints".equalsIgnoreCase(key)
                || "level".equalsIgnoreCase(key)
                || "world".equalsIgnoreCase(key)) {
            return owner.getAmount(key);
        }
        return Levelable.super.getAmount(key);
    }

    @Override
    public void setAmount(String key, long amount) {
        if ("missions".equalsIgnoreCase(key) || "missionpoints".equalsIgnoreCase(key)) {
            owner.setAmount(key, amount);
        } else {
            Levelable.super.setAmount(key, amount);
        }
    }

    @Override
    public long getBank() {
        return owner.getBank();
    }

    @Override
    public void setBank(long amount) {
        owner.setBank(amount);
    }

    public void setOwner(LandOwner<?, ?> owner) {
        this.owner = owner;
    }

    public NpcConfig getConfig() {
        return NpcManager.getInstance().getConfig(owner, type);
    }

    public String getNpcSkinName() {
        return npcSkinName;
    }

    public void setNpcSkinName(String npcSkinName) {
        this.npcSkinName = npcSkinName;
    }

    public boolean isAvailable() {
        return getConfig() != null;
    }

    public boolean hasLevels() {
        return NpcManager.getInstance().getLevels(owner, type).size() > 1;
    }

    public LandOwner<?, ?> getOwner() {
        return owner;
    }

    @Override
    public String getDbName() {
        return owner.getDbName() + "_npc";
    }
}
