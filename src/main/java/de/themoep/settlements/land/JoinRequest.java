package de.themoep.settlements.land;

import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.invites.Invite;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Targetable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class JoinRequest<S extends Targetable & DatabaseSerializable>  extends Invite<LandOwner<?, ?>, S> {

    public JoinRequest(LandOwner<?, ?> target, S source) {
        super(target, source);
    }

    public JoinRequest(String targetType, IdObject targetId, String sourceType, IdObject sourceId, long time) {
        super(targetType, targetId, sourceType, sourceId, time);
    }

    @Override
    public LandOwner<?, ?> getTarget() {
        return (LandOwner) getTargetAble(getTargetType(), getTargetId());
    }

    @Override
    public S getSource() {
        return (S) getTargetAble(getSourceType(), getSourceId());
    }

    private Targetable getTargetAble(String type, IdObject id) {
        return switch (type) {
            case "settlement" -> SettlementManager.getInstance().getFromId(id);
            case "empire" -> EmpireManager.getInstance().getEmpireFromId(id);
            case "resident" -> ResidentManager.getInstance().getResident(id);
            default -> null;
        };
    }

    @Override
    public String name() {
        return "joinrequest";
    }

    public static JoinRequest<?> deserialize(Map<String, Object> map) {
        String targetType = (String) map.get("targetType");
        IdObject targetId = IdObject.get(map.get("targetId"));

        String sourceType = (String) map.get("sourceType");
        IdObject sourceId = IdObject.get(map.get("sourceId"));

        long time = (long) map.get("time");

        return new JoinRequest(targetType, targetId, sourceType, sourceId, time);
    }


    @Override
    public String[] getReplacements(ConfigMessages configMessages, String... initial) {
        S source = getSource();
        if (source instanceof LandOwner<?, ?> owner) {
            return owner.getReplacements(configMessages, initial);
        } else if (source instanceof Resident res) {
            return res.getReplacements(initial);
        }
        return initial;
    }

    @Override
    public ItemStack getIcon() {
        S source = getSource();
        if (source instanceof LandOwner<?, ?> owner) {
            return owner.getIcon();
        } else if (source instanceof Resident res) {
            return ItemUtils.getHead(res.getUUID(), source.getName());
        }
        return new ItemStack(Material.PLAYER_HEAD);
    }
}
