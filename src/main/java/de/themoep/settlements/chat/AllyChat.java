package de.themoep.settlements.chat;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.firedev.chatchannels.api.ChannelLoadException;
import uk.firedev.chatchannels.api.ConfigChatChannel;
import uk.firedev.chatchannels.libs.daisylib.messages.replacer.Replacer;

public class AllyChat extends ConfigChatChannel {

    public AllyChat() throws ChannelLoadException {
        super("chat/ally.yml", "chat/ally.yml", SettlementsPlugin.getInstance(), true);
    }

    @Override
    public boolean shouldSendToTarget(@NonNull Player player, @NonNull Player target) {
        if (!super.shouldSendToTarget(player, target)) {
            return false;
        }
        Resident playerRes = ResidentManager.getInstance().getResident(player);
        if (!playerRes.hasSettlement()) {
            return false;
        }
        Resident targetRes = ResidentManager.getInstance().getResident(target);
        return playerRes.getSettlement().isAlly(targetRes);
    }

    @Override
    public @Nullable Replacer replacer(@NonNull Player player) {
        Resident res = ResidentManager.getInstance().getResident(player);
        Settlement settlement = res.getSettlement();
        return Replacer.replacer()
            .addReplacement("{settlement}", settlement.getName())
            .addReplacement("{rank}", res.getSettlementTitle());
    }

    @Override
    public boolean hasAccess(@NonNull Player player) {
        if (!super.hasAccess(player)) {
            return false;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        return res.hasSettlement() && res.getSettlement().hasAllies();
    }

}
