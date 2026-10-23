package de.themoep.settlements.invites;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ConfigMessagesOld;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SettlementInvite extends Invite<Resident, Settlement> {

    public SettlementInvite(Resident target, Settlement source) {
        super(target, source);
    }

    public SettlementInvite(IdObject targetId, IdObject sourceId, long time) {
        super("resident", targetId, "settlement", sourceId, time);
    }

    @Override
    public Resident getTarget() {
        return ResidentManager.getInstance().getResident(targetId);
    }

    @Override
    public Settlement getSource() {
        return SettlementManager.getInstance().getFromId(sourceId);
    }

    @Override
    public String name() {
        return "settlementinvite";
    }

    public static SettlementInvite deserialize(Map<String, Object> map) {
        IdObject targetId = IdObject.get(map.get("targetId"));

        IdObject sourceId = IdObject.get(map.get("sourceId"));

        long time = (long) map.get("time");

        return new SettlementInvite(targetId, sourceId, time);
    }


    @Override
    public String[] getReplacements(ConfigMessagesOld configMessages, String... initial) {
        Settlement source = getSource();
        return source.getReplacements(configMessages, initial);
    }

    @Override
    public ItemStack getIcon() {
        Resident chief = getSource().getChief();
        return ItemUtils.getHead(chief.getUUID(), chief.getName());
    }
}
