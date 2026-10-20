package de.themoep.settlements.utils.dialog.claim.states;

import de.themoep.settlements.utils.dialog.claim.ChunkState;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class ClaimedBySettlementState extends ChunkState {

    public ClaimedBySettlementState(@NonNull Player player, @NonNull Chunk chunk) {
        super(player, chunk, NamedTextColor.RED);
    }

    @Override
    public boolean click() {
        return false;
    }

    @Override
    public @NonNull String hoverKey() {
        return "dialog.claiming.chunk-state.claimed-by-settlement";
    }

}
