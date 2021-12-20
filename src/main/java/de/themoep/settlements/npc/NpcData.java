package de.themoep.settlements.npc;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.leveling.Levelable;
import de.themoep.settlements.perk.LevelPerk;
import de.themoep.settlements.perk.NpcPerk;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.Message;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class NpcData implements DatabaseSerializable, Levelable<NpcLevel> {
    private IdObject id = IdObject.get(null);
    private LandOwner<?, ?> owner;
    private int npcId;
    private final NpcType type;
    private int level = 0;
    private String npcSkinName = "";

    public NpcData(int npcId, NpcType type) {
        this.npcId = npcId;
        this.type = type;
    }

    protected NpcData(IdObject id, int npcId, NpcType type, int level, String npcSkinName) {
        this(npcId, type);
        this.id = id;
        this.level = level;
        this.npcSkinName = npcSkinName;
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
        // TODO: Per NPC save?
        owner.save();
    }

    @Override
    public LinkedHashMap<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("id", id.value());
        map.put("npcId", npcId);
        map.put("type", type);
        map.put("level", level);
        map.put("npcSkinName", npcSkinName);
        return map;
    }

    public static NpcData deserialize(Map<String, Object> map) {
        return new NpcData(
                IdObject.get(map.get("id")),
                (int) map.get("npcId"),
                NpcType.valueOf(((String) map.get("type")).toUpperCase(Locale.ROOT)),
                (int) map.get("level"),
                (String) map.get("npcSkinName")
        );
    }

    @Override
    public String name() {
        return type.getKey();
    }

    @Override
    public String getTag() {
        return type.getTag();
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

        setLevel(getLevel() + 1);
        if (owner instanceof Settlement) {
            SettlementManager.getInstance().removeLevelRequirements((Settlement) owner, resident);
        } else if (owner instanceof Empire) {
            EmpireManager.getInstance().removeLevelRequirements((Empire) owner, resident);
        }

        Message.get("levels.upgrade").tell(owner, "state", type.getTag(), "level", String.valueOf(level));

        List<String> perksDesc = new ArrayList<>();
        for (String perkName : getLevelInfo().getPerks()) {
            NpcPerk perk = NpcManager.getInstance().getPerk(owner, perkName);
            if (perk != null) {
                Message.get("levels.perk-unlocked").tell(owner, "perk", perk.getTag());
                owner.unlockPerk(resident, perk);
                perksDesc.add(perk.getTag());
            }
        }

        String mail = Message.get("levels.upgrade-mail").getReplacedText(
                "name", type.getTag(),
                "level", String.valueOf(level),
                "perks", String.join(", ", perksDesc)
        );
        for (MailProvider provider : SettlementsPlugin.getInstance().getMailProviders()) {
            for (Resident r : owner.getResidents()) {
                provider.sendMail(owner.getStateTag() + " " + getTag(), r, mail);
            }
        }
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
        owner.logDeposit(resident, deposited); // TODO: Per NPC
    }

    @Override
    public Collection<Resident> getOnlineResidents() {
        return owner.getOnlineResidents();
    }

    @Override
    public Map<String, Long> getVault() {
        return owner.getVault(); // TODO: Per NPC "vault"
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
}
