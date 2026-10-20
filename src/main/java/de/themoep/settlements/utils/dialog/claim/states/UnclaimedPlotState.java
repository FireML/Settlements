package de.themoep.settlements.utils.dialog.claim.states;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.utils.dialog.claim.ChunkState;
import de.themoep.settlements.world.SimpleChunkLocation;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class UnclaimedPlotState extends ChunkState {

    public UnclaimedPlotState(@NonNull Player player, @NonNull Chunk chunk) {
        super(player, chunk, NamedTextColor.DARK_PURPLE);
    }

    @Override
    public boolean click() {
        if (land.getPlotOwner() == null) {
            LandManager.getInstance().setPlotOwner(land, resident.getUUID());
            return true;
        }
        return false;
    }

    @Override
    public @NonNull String hoverKey() {
        return "dialog.claiming.chunk-state.unclaimed-plot";
    }

}
