package de.themoep.settlements.utils.dialog.claim.states;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.utils.dialog.claim.ChunkState;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class ClaimedByYouState extends ChunkState {

    public ClaimedByYouState(@NonNull Player player, @NonNull Chunk chunk) {
        super(player, chunk, NamedTextColor.YELLOW);
    }

    @Override
    public boolean click() {
        if (land.isOwnedBy(resident)) {
            LandManager.getInstance().setPlotOwner(land, null);
            return true;
        }
        return false;
    }

    @Override
    public @NonNull String hoverKey() {
        return "dialog.claiming.chunk-state.claimed-by-you";
    }

}
