package de.themoep.settlements.utils.dialog.claim.states;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.utils.dialog.claim.ChunkState;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class ClaimedPlotState extends ChunkState {

    public ClaimedPlotState(@NonNull Player player, @NonNull Chunk chunk) {
        super(player, chunk, NamedTextColor.LIGHT_PURPLE);
    }

    @Override
    public boolean click() {
        return false;
    }

    @Override
    public @NonNull String hoverKey() {
        return "dialog.claiming.chunk-state.claimed-plot";
    }

}
