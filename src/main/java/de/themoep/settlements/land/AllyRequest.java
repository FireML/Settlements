package de.themoep.settlements.land;

import de.themoep.settlements.invites.Invite;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ConfigMessagesOld;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class AllyRequest extends Invite<Settlement, Settlement> {

    public AllyRequest(Settlement target, Settlement source) {
        super(target, source);
    }

    public AllyRequest(String targetType, IdObject targetId, String sourceType, IdObject sourceId, long time) {
        super(targetType, targetId, sourceType, sourceId, time);
    }

    @Override
    public Settlement getTarget() {
        return getSettlement(getTargetType(), getTargetId());
    }

    @Override
    public Settlement getSource() {
        return getSettlement(getSourceType(), getSourceId());
    }

    private Settlement getSettlement(String type, IdObject id) {
        return "settlements".equals(type) ? SettlementManager.getInstance().getFromId(id) : null;
    }

    @Override
    public String name() {
        return "settlementinvite";
    }

    public static AllyRequest deserialize(Map<String, Object> map) {
        String targetType = (String) map.get("targetType");
        IdObject targetId = IdObject.get(map.get("targetId"));

        String sourceType = (String) map.get("sourceType");
        IdObject sourceId = IdObject.get(map.get("sourceId"));

        long time = (long) map.get("time");

        return new AllyRequest(targetType, targetId, sourceType, sourceId, time);
    }


    @Override
    public String[] getReplacements(ConfigMessagesOld configMessages, String... initial) {
        return getSource().getReplacements(configMessages, initial);
    }

    @Override
    public ItemStack getIcon() {
        Resident chief = getSource().getChief();
        return ItemUtils.getHead(chief.getUUID(), chief.getName());
    }
}
