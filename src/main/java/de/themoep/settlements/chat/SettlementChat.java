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

public class SettlementChat extends ConfigChatChannel {

    public SettlementChat() throws ChannelLoadException {
        super("chat/settlement.yml", "chat/settlement.yml", SettlementsPlugin.getInstance(), true);
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
        return playerRes.getSettlement().equals(targetRes.getSettlement());
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
        return ResidentManager.getInstance().doesResidentHaveSettlement(player);
    }

}
