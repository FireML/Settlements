package de.themoep.settlements.land;

import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.invites.Invite;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class AllyRequest extends Invite<LandOwner, LandOwner> {

    public AllyRequest(LandOwner target, LandOwner source) {
        super(target, source);
    }

    public AllyRequest(String targetType, IdObject targetId, String sourceType, IdObject sourceId, long time) {
        super(targetType, targetId, sourceType, sourceId, time);
    }

    @Override
    public LandOwner getTarget() {
        return getLandOwner(getTargetType(), getTargetId());
    }

    @Override
    public LandOwner getSource() {
        return getLandOwner(getSourceType(), getSourceId());
    }

    private LandOwner getLandOwner(String type, IdObject id) {
        return switch (type) {
            case "settlement" -> SettlementManager.getInstance().getFromId(id);
            case "empire" -> EmpireManager.getInstance().getEmpireFromId(id);
            default -> null;
        };
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
    public String[] getReplacements(ConfigMessages configMessages, String... initial) {
        return getSource().getReplacements(configMessages, initial);
    }

    @Override
    public ItemStack getIcon() {
        Resident chief = getSource().getChief();
        return ItemUtils.getHead(chief.getUUID(), chief.getName());
    }
}
