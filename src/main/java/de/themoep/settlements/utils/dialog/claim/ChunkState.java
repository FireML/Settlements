package de.themoep.settlements.utils.dialog.claim;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.config.ConfigMessage;
import de.themoep.settlements.utils.dialog.claim.states.ClaimedByAllyState;
import de.themoep.settlements.utils.dialog.claim.states.ClaimedBySettlementState;
import de.themoep.settlements.utils.dialog.claim.states.ClaimedByYouState;
import de.themoep.settlements.utils.dialog.claim.states.ClaimedByYourSettlementState;
import de.themoep.settlements.utils.dialog.claim.states.ClaimedPlotState;
import de.themoep.settlements.utils.dialog.claim.states.UnclaimedPlotState;
import de.themoep.settlements.utils.dialog.claim.states.UnclaimedState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.messages.replacer.Replacer;

import static de.themoep.settlements.utils.MessageUtils.yesOrNo;

public abstract class ChunkState {

    protected final Player player;
    protected final Resident resident;
    protected final Chunk chunk;
    protected final OwnedLand land;

    private final Component square;

    public ChunkState(@NonNull Player player, @NonNull Chunk chunk, @NonNull TextColor color) {
        this.player = player;
        this.resident = ResidentManager.getInstance().getResident(player);
        this.chunk = chunk;
        this.land = LandManager.getInstance().getLand(chunk);

        this.square = Component.text('█', color);
    }

    public abstract boolean click();

    public abstract @NonNull String hoverKey();

    public @NonNull Component getSquare() {
        Replacer replacer = Replacer.component()
            .addReplacement("{ownerstate}", land == null ? "None" : land.getSettlement().getStateTag())
            .addReplacement("{owner}", land == null ? "None" : land.getSettlement().getName())
            .addReplacement("{embassy}", land == null ? "No" : yesOrNo(land.isEmbassy()))
            .addReplacement("{plot}", land == null ? "No" : yesOrNo(land.isPlot()))
            .addReplacement("{forsale}", land == null ? "No" : yesOrNo(land.isForSale()))
            .addReplacement("{plotowner}", land == null || land.getPlotOwner() == null ? "None" : land.getPlotOwnerResident().getName());
        Component hover = ConfigMessage.get(hoverKey())
            .toSingleMessage()
            .replace(replacer)
            .get();
        return square
            .hoverEvent(HoverEvent.showText(hover))
            .clickEvent(ClickEvent.callback(_ -> {
                if (click()) {
                    ClaimMap.open(player);
                }
            }));
    }

    public static ChunkState get(@NonNull Player player, @NonNull Chunk chunk) {
        OwnedLand land = LandManager.getInstance().getLand(chunk);
        Resident resident = ResidentManager.getInstance().getResident(player);
        if (land == null) {
            return new UnclaimedState(player, chunk);
        }
        Settlement owner = land.getSettlement();
        if (owner == null) {
            return new UnclaimedState(player, chunk);
        }
        if (land.isPlot()) {
            if (land.isPlotOwner(resident)) {
                return new ClaimedByYouState(player, chunk);
            } else if (land.getPlotOwner() != null) {
                return new ClaimedPlotState(player, chunk);
            } else {
                return new UnclaimedPlotState(player, chunk);
            }
        }
        if (owner.isMember(resident)) {
            return new ClaimedByYourSettlementState(player, chunk);
        }
        if (owner.isAlly(resident)) {
            return new ClaimedByAllyState(player, chunk);
        }
        return new ClaimedBySettlementState(player, chunk);
    }

}
